<?php

	include_once('./functions.php');

    global $const;

	$db_conn = NULL;

		/* Establishes a connection to the PostgreSQL database. Returns NULL if connection failed. */
	function connect() {
	
		global $db_conn;
		// no db object
		if (is_null($db_conn)) {
			// reload configuration
			load_conf(true);
			$conf_arr = $GLOBALS['web_conf']['dbconf'];
			
			// create the connection string
			$dsn = $conf_arr['db_driver'] . 
					':host=' . $conf_arr['db_host'] .
					';port=' . $conf_arr['db_port'] .
					';dbname=' . $conf_arr['db_name'] .
                    ';user=' . $conf_arr['db_user'] .
                    ';password=' . $conf_arr['db_pass'];
			try {
				$db_conn = new PDO($dsn);
			} catch (Exception $e) {
				//unable to connect, go to error page or 404 page
                //print "Exception: " . $e;
			}
			}
	}
    
    // Returns the global DB connection. If it is null, try to make a connection.
    function get_db_conn() {
        global $db_conn;

        if (is_null($db_conn)) { connect(); }

        return $db_conn;
    }

    function get_schedules() {

        $db_conn = get_db_conn();

        try {
            $sql = "SELECT schedules.sid, schedules.comment,"
				. " days.day, days.did "
                . " FROM ptaweb.schedules JOIN ptaweb.days "
                . " ON (ptaweb.schedules.did=ptaweb.days.did AND "
                . " ptaweb.schedules.rsid=ptaweb.days.rsid)";

            $pdostm = $db_conn->prepare($sql);
            $success = $pdostm->execute();

            if ($success) {
                return $pdostm->fetchAll();
            } else {
                debug_log("Query $sql failed.");
                return NULL;
            }

        } catch (Exception $e) {
            debug_log("Query $sql threw exception: " . $e->getMessage());
            return NULL;
        }
    }

	function delete_schedules($rsid) {

        $db_conn = get_db_conn();

        try {
            $sql = "DELETE FROM ptaweb.schedules WHERE rsid = :rsid";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
            
            return $pdostm->execute();
        } catch (Exception $e) {
            return NULL;
        }
    }

    function get_sched_times($sid) {

        $db_conn = get_db_conn();

        try {
            
            $sql = "SELECT reg_session.aft_start, reg_session.eve_start, reg_session.meet_len "
                . " FROM ptaweb.schedules JOIN ptaweb.reg_session "
                . " ON (ptaweb.schedules.rsid=ptaweb.reg_session.rsid) "
                . " WHERE schedules.sid = :sid";

            $pdostm = $db_conn->prepare($sql);
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);

            $success = $pdostm->execute();

            if ($success) {
                return $pdostm->fetch();
            } else {
                debug_log("Query $sql failed.");
                return NULL;
            }

        } catch (Exception $e) {
            debug_log("Query $sql threw exception: " . $e->getMessage());
            return NULL;
        }
    }

/* Admin Functions ***********************************************************/

	function get_admins() {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = "SELECT * FROM ptaweb.admin";
        
        $pdostm = $db_conn->prepare($sql);
        
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetchAll();
		} else {
			return NULL;
		}
	}

	function get_admin_user($user, $passwd) {
	
		$db_conn = get_db_conn();

        try {

            if (!is_null($passwd)) { 
                $pw_sql = " AND passwd = :passwd ";
            } else {
                $pw_sql = " ";
            }

    		$sql = "SELECT * FROM ptaweb.admin " .
	    			"WHERE username = :user" . $pw_sql;

		    $pdostm = $db_conn->prepare($sql);
    		$pdostm->bindParam(':user', $user, PDO::PARAM_STR);

            if (!is_null($passwd)) { 
    		    $pdostm->bindParam(':passwd', $passwd, PDO::PARAM_STR);
            }

            debug_log("$sql with $user:$passwd");

    		$success = $pdostm->execute();

            if($success) {
		    	return $pdostm->fetch();
    		} else {
                debug_log('get_admin_user: no users found');
		    	return null;
    		}

        } catch (Exception $e) {
            return null;
        }
	
	}
	
    /* Adds a new administrator user. Username must be unique.
     */
	function add_admin_user($user, $passwd) {

		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "INSERT INTO ptaweb.admin VALUES (:user, :passwd)";
	
			$pdostm = $db_conn->prepare($sql);
            
            $passwd = md5($passwd);
			$pdostm->bindParam(':user', $user, PDO::PARAM_STR, 20);
			$pdostm->bindParam(':passwd', $passwd, PDO::PARAM_STR);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	
	}

    /* Pass passwd as plaintext, we md5 it here
     */	
	function update_admin_user($user, $passwd) {
		
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "UPDATE ptaweb.admin " .
					"SET passwd = :passwd WHERE (username = :user)";
	
			$pdostm = $db_conn->prepare($sql);

            $passwd = md5($passwd);
			$pdostm->bindParam(':user', $user, PDO::PARAM_STR, 20);
			$pdostm->bindParam(':passwd', $passwd, PDO::PARAM_STR);
			
			$ret = $pdostm->execute();
            if ($ret == 0) {
                print_r($pdostm->errorInfo());
            }
            return $ret;
		} catch (Exception $e) {

                print_r($pdostm->errorInfo());
			return null;
		}
	
	}
	
	function delete_admin_user($user) {
		
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "DELETE ptaweb.admin " .
					"WHERE (username = :user)";
	
			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':user', $user, PDO::PARAM_STR, 20);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	
	}


/* Parent Functions **********************************************************/

	function get_parent_user($user, $pass) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
		$sql = "SELECT * FROM ptaweb.parents " .
				"WHERE username = :user AND passwd = :pass";

		$pdostm = $db_conn->prepare($sql);

		$pdostm->bindParam(':user', $user, PDO::PARAM_STR);
		$pdostm->bindParam(':pass', $pass, PDO::PARAM_STR);
		$success = $pdostm->execute();
        if($success) {
			return $pdostm->fetch();
		} else {
			return NULL;
		}
	}
	
    function get_parent($id) {

		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = "SELECT * FROM ptaweb.parents WHERE (pid = :id)";
       
        $pdostm = $db_conn->prepare($sql);
        $pdostm->bindParam(':id', $id, PDO::PARAM_INT);
        
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetch();
		} else {
			return NULL;
		}

    }

	function get_parents() {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = "SELECT * FROM ptaweb.parents ORDER BY lname";
        
        $pdostm = $db_conn->prepare($sql);
        
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetchAll();
		} else {
			return NULL;
		}
	}

    /* Adds a new parent user to the DB. Pass the password in in plaintext,
     * we will md5 it here, before doing executing the SQL statement.n could not be updated.
     */
	function add_parent_user($fname, $lname, $username, $passwd, $email, $tb) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
		try {

			$sql = "INSERT INTO ptaweb.parents " . 
					"(pid, fname, lname, username, passwd, email, tb) " .
					"VALUES " .
					"(default, :fname, :lname, :user, :passwd, :email, :tb)"; 

			$pdostm = $db_conn->prepare($sql);

            $passwd = md5($passwd);
			$pdostm->bindParam(':fname', $fname, PDO::PARAM_STR);
			$pdostm->bindParam(':lname', $lname, PDO::PARAM_STR);
			$pdostm->bindParam(':user', $username, PDO::PARAM_STR, 40);
			$pdostm->bindParam(':passwd', $passwd, PDO::PARAM_STR);
			$pdostm->bindParam(':email', $email, PDO::PARAM_STR);
			$pdostm->bindParam(':tb', $tb, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}

	}


    /* Update the parent (specified by pid). Give passwd in plaintext, or set
     * it as NULL to leave the password unchanged.
     */
    function update_parent($pid, $fname, $lname, $username, $passwd,
							$email, $tb = null) {
        
        global $db_conn;
        if (is_null($db_conn)) {
            connect();
        }

        try {

            if (!is_null($passwd)) { 
                $pw_sql = ", passwd = :passwd ";
            } else {
                $pw_sql = " ";
            }

            if (!is_null($tb)) { 
                $tb_sql = ", tb = :tb ";
            } else {
                $tb_sql = " ";
            }
       
            $sql = "UPDATE ptaweb.parents " . 
                    "SET fname = :fname, lname = :lname, email = :email, " .
					"username = :username $pw_sql $tb_sql" .
					"WHERE (pid = :pid)"; 

            $pdostm = $db_conn->prepare($sql);

            $pdostm->bindParam(':fname', $fname, PDO::PARAM_STR);
            $pdostm->bindParam(':lname', $lname, PDO::PARAM_STR);
            $pdostm->bindParam(':username', $username, PDO::PARAM_STR, 40);
            $pdostm->bindParam(':email', $email, PDO::PARAM_STR);
            $pdostm->bindParam(':pid', $pid, PDO::PARAM_INT);

            if (!(is_null($passwd))) {
                $passwd = md5($passwd);
                $pdostm->bindParam(':passwd', $passwd, PDO::PARAM_STR);
            }
            if (!(is_null($tb))) {
            	$pdostm->bindParam(':tb', $tb, PDO::PARAM_INT);
            }
           
			$ret = $pdostm->execute();
            if ($ret == 0) {
                print_r($pdostm->errorInfo());
            }
            return $ret;
        } catch (Exception $e) {
            return null;
        }
    }

    /* Update the parent's timeblock (specified by pid).
     */
    function update_parent_tb($pid, $tb) {
        
        global $db_conn;
        if (is_null($db_conn)) {
            connect();
        }

        try {

            $sql = "UPDATE ptaweb.parents " . 
                    "SET tb = :tb WHERE (pid = :pid)"; 

            $pdostm = $db_conn->prepare($sql);

            $pdostm->bindParam(':pid', $pid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $tb, PDO::PARAM_INT);

			$ret = $pdostm->execute();
            if ($ret == 0) {
                print_r($pdostm->errorInfo());
            }
            return $ret;
        } catch (Exception $e) {
            return null;
        }
    }

	function delete_parent_user($pid) {
		
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
		try {
		
			$sql = "DELETE FROM ptaweb.parents " .
					"WHERE (pid = :id)"; 

			$pdostm = $db_conn->prepare($sql);
            $pdostm->bindParam(':id', $pid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	}


/* Teacher Functions *********************************************************/

	function get_teachers() {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = "SELECT * FROM ptaweb.teachers ORDER BY lname";
        
        $pdostm = $db_conn->prepare($sql);
        
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetchAll();
		} else {
			return NULL;
		}
	}

    function get_teacher($id) {

		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = "SELECT * FROM ptaweb.teachers WHERE (tid = :id)";
       
        $pdostm = $db_conn->prepare($sql);
        $pdostm->bindParam(':id', $id, PDO::PARAM_INT);
        
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetch();
		} else {
			return NULL;
		}
    }

	function add_teacher($fname, $lname) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {

			$sql = "INSERT INTO ptaweb.teachers " . 
					"(tid, fname, lname) " .
					"VALUES (default, :fname, :lname)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':fname', $fname, PDO::PARAM_STR);
			$pdostm->bindParam(':lname', $lname, PDO::PARAM_STR);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	}

	function update_teacher($tid, $fname, $lname) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        try {
			$sql = "UPDATE ptaweb.teachers " . 
					"SET fname = :fname, lname = :lname " .
					"WHERE (tid = :tid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':fname', $fname, PDO::PARAM_STR);
			$pdostm->bindParam(':lname', $lname, PDO::PARAM_STR);
			$pdostm->bindParam(':tid', $tid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	}

	function delete_teacher($tid) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {
			$sql = "DELETE FROM ptaweb.teachers " . 
					"WHERE (tid = :tid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':tid', $tid, PDO::PARAM_INT);
			
			$ret = $pdostm->execute();
            if ($ret == 0) {
                print_r($pdostm->errorInfo());
            }
            return $ret;
		} catch (Exception $e) {
			return null;
		}
	}


/* Session management ********************************************************/

	function add_reg_session($startts, $endts, $aft_start, $aft_end,
								$eve_start, $eve_end, $meet_len) {
	
        global $const;
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {
			$sql = "INSERT INTO ptaweb.reg_session " . 
					"(rsid, startts, endts, status, " .
					"aft_start, aft_end, eve_start, eve_end, meet_len) " .
					"VALUES (default, :startts, :endts, " .
					$const['reg_status']['new'] . ", " .
					":aft_start, :aft_end, " .
					":eve_start, :eve_end, :meet_len)"; 
					
			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':startts', $startts, PDO::PARAM_STR);
			$pdostm->bindParam(':endts', $endts, PDO::PARAM_STR);
			$pdostm->bindParam(':aft_start', $aft_start, PDO::PARAM_STR);
			$pdostm->bindParam(':aft_end', $aft_end, PDO::PARAM_STR);
			$pdostm->bindParam(':eve_start', $eve_start, PDO::PARAM_STR);
			$pdostm->bindParam(':eve_end', $eve_end, PDO::PARAM_STR);
			$pdostm->bindParam(':meet_len', $meet_len, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	}

	function get_reg_sessions($status) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
		
		$sql = "SELECT * FROM ptaweb.reg_session WHERE (status = :status)";

		$pdostm = $db_conn->prepare($sql);

		$pdostm->bindParam(':status', $status, PDO::PARAM_INT);
		
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetchAll();
		} else {
			return NULL;
		}
	}

	// returns the current session that can be edited and opened by admin
    function get_curr_reg_session() {
        global $const;    
    	global $db_conn;

		if (is_null($db_conn)) {
			connect();
		}
		// get new, open, or closed (but not archived) session
        $sql = 'SELECT * FROM ptaweb.reg_session '
            . 'WHERE (status = :new) OR (status = :open) '
			. 'OR (status = :closed)';

		$pdostm = $db_conn->prepare($sql);

		$pdostm->bindParam(':new', $const['reg_status']['new'], PDO::PARAM_INT);
		$pdostm->bindParam(':open', $const['reg_status']['open'],
							PDO::PARAM_INT);
		$pdostm->bindParam(':closed', $const['reg_status']['closed'],
							PDO::PARAM_INT);


        $success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetch();
		} else {
			return NULL;
		}
    }

    /* Returns an array containing information pertinent to the given rsid
     */
    function session_info($rsid) {
        global $const;
        $db_conn = get_db_conn();
        
        $sql = 'SELECT *  FROM ptaweb.reg_session '
            . 'INNER JOIN ptaweb.days ON ptaweb.reg_session.rsid = ptaweb.days.rsid '
            . 'WHERE ptaweb.reg_session = :rsid' ;

        $pdostm = $db_conn->prepare($sql);
        return $pdostm->fetchAll();
    }

	// returns the current session that is open for registration to the parents
	function get_new_session() {
        global $const;
		return get_reg_sessions($const['reg_status']['new']);
	}

	function get_open_session() {
        global $const;
		return get_reg_sessions($const['reg_status']['open']);
	}

	function get_closed_session() {
        global $const;
		return get_reg_sessions($const['reg_status']['closed']);
	}
	
	function get_archived_sessions() {
        global $const;
		return get_reg_sessions($const['reg_status']['archived']);
	}

	function get_session_dates($rsid) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = 'SELECT * FROM ptaweb.reg_session WHERE (rsid = :rsid)';

        $pdostm = $db_conn->prepare($sql);

		$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
		
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetch();
		} else {
			return NULL;
		}
	}

	function update_reg_session($rsid, $startts, $endts, $aft_start, $aft_end,
								$eve_start, $eve_end, $meet_len) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {
			$sql = "UPDATE ptaweb.reg_session " . 
					"SET startts = :startts, ".
					"endts = :endts, " .
					"aft_start = :aft_start, aft_end = :aft_end, " .
					"eve_start = :eve_start, eve_end = :eve_end, " .
					"meet_len = :meet_len " .
					"WHERE (rsid = :rsid)"; 
					
			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':startts', $startts, PDO::PARAM_STR);
			$pdostm->bindParam(':endts', $endts, PDO::PARAM_STR);
			$pdostm->bindParam(':aft_start', $aft_start, PDO::PARAM_STR);
			$pdostm->bindParam(':aft_end', $aft_end, PDO::PARAM_STR);
			$pdostm->bindParam(':eve_start', $eve_start, PDO::PARAM_STR);
			$pdostm->bindParam(':eve_end', $eve_end, PDO::PARAM_STR);
			$pdostm->bindParam(':meet_len', $meet_len, PDO::PARAM_INT);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			
			return $pdostm->execute();
		} catch (Exception $e) {
			
			return null;
		}
	}

	function update_session_status($rsid, $status) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {
			$sql = "UPDATE ptaweb.reg_session " . 
					"SET status = :status" .
					"WHERE (rsid = :rsid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':status', $status, PDO::PARAM_INT);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			print_r($pdostm->getInfo());
			echo $e->getMessage();
			return null;
		}
	}
	
	function close_reg_session($rsid) {
        global $const;
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		try {
			$sql = "UPDATE ptaweb.reg_session " . 
					"SET status = :status, endts = :endts " .
					"WHERE (rsid = :rsid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':status', $const['reg_status']['closed'],
								PDO::PARAM_INT);
			$pdostm->bindParam(':endts', strftime('%F %T'), PDO::PARAM_STR);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	}

	function open_reg_session($rsid) {
        global $const;
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		try {
			$sql = "UPDATE ptaweb.reg_session " . 
					"SET status = :status, startts = :startts " .
					"WHERE (rsid = :rsid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':status', $const['reg_status']['open'],
								PDO::PARAM_INT);
			$pdostm->bindParam(':startts', strftime('%F %T'), PDO::PARAM_STR);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);

			return $pdostm->execute();
		} catch (Exception $e) {
			print_r($e);
			return null;
		}
	}

	function delete_reg_session($rsid) {
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "DELETE FROM ptaweb.reg_session " . 
					"WHERE (rsid = :rsid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	}


	// returns the days when parents can request interviews
	function get_session_parent_days($rsid) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
		
        $sql = 'SELECT * FROM ptaweb.days WHERE (rsid = :rsid)';

        $pdostm = $db_conn->prepare($sql);

		$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
		
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetchAll();
		} else {
			return NULL;
		}
	}


    /* Return parent preferences given a parent id
     */
	function get_parent_prefs($pid) {
	
		$db_conn = get_db_conn();
		
		$sql = "SELECT email FROM ptaweb.parents WHERE pid = :pid";

		$pdostm = $db_conn->prepare($sql);

		$pdostm->bindParam(':pid', $pid, PDO::PARAM_STR);
		
		$success = $pdostm->execute();
		
        if($success) {
	        return $pdostm->fetch();
		} else {
			return NULL;
		}
	}
	
	function add_parent_pref($user, $day, $rsid) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {
			$sql = "INSERT INTO ptaweb.parent_prefs " . 
					"(pid, day, rsid) " .
					"VALUES ( (SELECT pid FROM ptaweb.parents " .
								"WHERE (username = :user)" .
							"), :day, :rsid)"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':user', $user, PDO::PARAM_STR);
			$pdostm->bindParam(':day', $day, PDO::PARAM_INT);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}

	}

	function update_parent_pref($user, $day, $rsid) {
	
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}
	
		try {
			$sql = "UPDATE ptaweb.parent_prefs " . 
					"SET day = :day, " .
					"WHERE (rsid = :rsid) AND (pid = " .
					"(SELECT pid FROM ptaweb.parents WHERE (username = :user)))"; 

			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':user', $user, PDO::PARAM_STR);
			$pdostm->bindParam(':day', $day, PDO::PARAM_INT);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			$pdostm->execute();
            return $pdostm->fetchAll();
		} catch (Exception $e) {
			return null;
		}
	}

    /* Return all of the requests that the given parent has entered into the DB
     */
    function get_requests_by_parent($pid, $rsid) {
        $db_conn = get_db_conn();

        try {
            $sql = "SELECT * FROM ptaweb.requests " .
					"WHERE (pid = :pid) AND (rsid = :rsid)";

            $pdostm = $db_conn->prepare($sql);

            $pdostm->bindParam(':pid', $pid, PDO::PARAM_INT);
            $pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);

            $pdostm->execute();
            return $pdostm->fetchAll();
        } catch (Exception $e) {
            return null;
        }
    }

    /* Delete the request with the given ID
     */
    function delete_request($rid) {
        $db_conn = get_db_conn();

        try {
            $sql = "DELETE FROM ptaweb.requests WHERE rid = :rid";
            $pdostm = $db_conn->prepare($sql);
            $pdostm->bindParam(':rid', $rid, PDO::PARAM_INT);
            return $pdostm->execute();
        } catch (Exception $e) {
            return null;
        }
    }

    /* Insert an appointment request with the given data.
     */
    function insert_request($pid, $tid, $did, $rsid) {
        $db_conn = get_db_conn();

        try {
            $sql = "INSERT INTO ptaweb.requests (rid, pid, tid, did, rsid) "
                . "VALUES (default, :pid, :tid, :did, :rsid)";
            $pdostm = $db_conn->prepare($sql);
            $pdostm->bindParam(':pid', $pid, PDO::PARAM_INT);
            $pdostm->bindParam(':tid', $tid, PDO::PARAM_INT);
            $pdostm->bindParam(':did', $did, PDO::PARAM_INT);
            $pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
            return $pdostm->execute();
        } catch (Exception $e) {
            return null;
        }
    }
	
	function add_date($rsid, $day) {

		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "INSERT INTO ptaweb.days " .
					"VALUES (default, :day, :rsid, '')";
	
			$pdostm = $db_conn->prepare($sql);
            
			$pdostm->bindParam(':day', $day, PDO::PARAM_STR);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);

			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	
	}

    /* Pass passwd as plaintext, we md5 it here
     */	
	function update_date($rsid, $day) {
		
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "UPDATE ptaweb.days " .
					"SET day = :day WHERE (rsid = :rsid)";
	
			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':day', $day, PDO::PARAM_STR);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {		
			return null;
		}
	
	}
	
	function delete_date($rsid, $day) {$pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
		
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "DELETE ptaweb.days " .
					"WHERE (rsid = :rsid) AND (day = :day)";
	
			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':day', $day, PDO::PARAM_STR);
			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	
	}

	function delete_sess_dates($rsid) {
		
		global $db_conn;
		if (is_null($db_conn)) {
			connect();
		}

		try {
			$sql = "DELETE ptaweb.days " .
					"WHERE (rsid = :rsid)";
	
			$pdostm = $db_conn->prepare($sql);

			$pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
			
			return $pdostm->execute();
		} catch (Exception $e) {
			return null;
		}
	
	}
	
	function get_num_aft_appts($sid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "SELECT MAX(slot) AS num FROM ptaweb.appointments " .
            		"WHERE (sid = :sid) AND (tb = :tb)";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $const['time_block']['afternoon'],
            					PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetch();
        } catch (Exception $e) {
            return null;
        }
	}
	
	function get_num_eve_appts($sid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "SELECT MAX(slot) AS num FROM ptaweb.appointments " .
            		"WHERE (sid = :sid) AND (tb = :tb)";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $const['time_block']['evening'],
            					PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetch();
        } catch (Exception $e) {
            return null;
        }
	}
	
	function query_aft_appts($sid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "SELECT * FROM ptaweb.appointments " .
            		"WHERE (sid = :sid) AND (tb = :tb)";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $const['time_block']['afternoon'],
            					PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetchAll();
        } catch (Exception $e) {
            return null;
        }
	}

	function query_eve_appts($sid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "SELECT * FROM ptaweb.appointments " .
            		"WHERE (sid = :sid) AND (tb = :tb)";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $const['time_block']['evening'],
            					PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetchAll();
        } catch (Exception $e) {
            return null;
        }
	}

	function query_aft_appts_parent($sid, $pid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "SELECT * FROM ptaweb.appointments " .
            		"WHERE (sid = :sid) AND (tb = :tb) AND (pid = :pid) ".
            		"ORDER BY slot";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
            $pdostm->bindParam(':pid', $pid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $const['time_block']['afternoon'],
            					PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetchAll();
        } catch (Exception $e) {
            return null;
        }
	}

	function query_eve_appts_parent($sid, $pid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "SELECT * FROM ptaweb.appointments " .
            		"WHERE (sid = :sid) AND (tb = :tb) AND (pid = :pid)".
            		"ORDER BY slot";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':sid', $sid, PDO::PARAM_INT);
            $pdostm->bindParam(':pid', $pid, PDO::PARAM_INT);
            $pdostm->bindParam(':tb', $const['time_block']['evening'],
            					PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetchAll();
        } catch (Exception $e) {
            return null;
        }
	}

	function get_unsched_requests($rsid) {
		global $const;
		$db_conn = get_db_conn();

        try {
            $sql = "(SELECT tid, pid FROM ptaweb.requests " .
            		"WHERE (requests.rsid = :rsid)) " .
	            	"EXCEPT (SELECT tid, pid " .
	            	"FROM ptaweb.appointments JOIN ptaweb.schedules " .
	            	"ON (appointments.sid = schedules.sid) " .
	            	"WHERE (schedules.rsid = :rsid))";
            
            $pdostm = $db_conn->prepare($sql);
            
            $pdostm->bindParam(':rsid', $rsid, PDO::PARAM_INT);
            
            $pdostm->execute();
            
            return $pdostm->fetchAll();
        } catch (Exception $e) {
            return null;
        }

	}

	function get_unsched_requests_teach($sid, $tid) {

	}

?>
