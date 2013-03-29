<?php

error_reporting(0);

include_once("./db_conn.php");

$urls = array(
'index' => 'index.php',
'admin' =>  array(
    'login' => 'admin-login.php',
    'home'  => 'admin-home.php',
    'change-pw' => 'admin-change-pw.php'),
'parent' => array(
    'login' => 'index.php',
    'home'  => 'home.php',
    'settings' => 'settings.php',
    'schedule' => 'schedule.php'));

$const = array(
'reg_status' => array(
    'new' => 1,
    'open' => 2,
    'closed' => 3,
    'archived' => 4),
'time_block' => array(
    'afternoon' => 1,
    'evening' => 2,
    'both' => 0),
'max_interviews' => 9);

$banner_title_prefix='SCS Interview System';
$html_header_title_prefix='SCS Interview System';

function debug_log($msg) {
    if ($GLOBALS['web_conf']['debug']) {
        $_SESSION["debug_log"] = "<li>$msg</li>\n" . $_SESSION["debug_log"];
    }
}

function html_header($title) {

    if ($title == '') {
        $header_title = $GLOBALS['html_header_title_prefix'];
    } else {
        $header_title = sprintf("%s: %s", $GLOBALS['html_header_title_prefix'], $title);
    }

    printf(
'<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link rel="stylesheet" href="./css/style.css" type="text/css">
    <title>%s</title>
</head>
<body onload="setJSCookie()" >',
$header_title);
}

function html_footer() {
	echo '</div> <!-- /content -->
    <ul id="debug_log">' . $_SESSION['debug_log'] . '</ul>
</body>
</html>';
}


function html_banner($title) {

    if (logged_in('parent')) {
        $usr_msg = 'Welcome, ' . $_SESSION['parent']['fname']
            . ' ' . $_SESSION['parent']['lname'] . ' - <a href="logout.php" id="logout_text">(Logout)</a>';
    } else if (logged_in('admin')) {
        $usr_msg = 'Welcome, ' . $_SESSION['admin']['username']
            . ' - <a href="logout.php" id="logout_text">(Logout)</a>';
    } else {
        $usr_msg = '';
    }

    if ($title == '') {
        $banner_title = $GLOBALS['banner_title_prefix'];
    } else {
        $banner_title = sprintf("%s: %s", $GLOBALS['banner_title_prefix'], $title);
    }

    printf(
'<div id="banner" class="bannerClass">
<h1 id="banner1" class="banner1Class">%s</h1>
<div id="banner2" class="banner2Class" align="right">%s</div>
<ul class="nav_bar">',
$banner_title, $usr_msg);

    
	if(logged_in('parent')) {
		echo '<li><a href="home.php">Home</a></li>
		<li><a href="schedule.php">Schedule</a></li>
		<li><a href="settings.php">Settings</a></li>';
	} else if (logged_in('admin')) {
    	echo '<li><a href="admin-home.php">Home</a></li>
		<li><a href="admin-session.php">Session</a></li>
		<li><a href="admin-parents.php">Parents</a></li>
		<li><a href="admin-teachers.php">Teachers</a></li>
        <li><a href="admin-admin.php">Administrators</a></li>
		<li><a href="admin-schedules.php">Schedules</a></li>
		<li><a href="admin-tocall.php">To-Call List</a></li>
		<li><a href="admin-change-pw.php">Change Password</a></li>';
	}

    print '</ul></div> <!--banner -->
<div id="content">';
}


/* Set error message in the session
 */
function set_error_msg($key, $msg) {
    $_SESSION['error'][$key] = $msg;
}

/* Print error message (if any) and unsets it.
 */
function emit_error_msg($key) {
    if (isset($_SESSION['error'][$key])) {
        print '<div class="error_msg"><p>' . $_SESSION['error'][$key] . '</p></div>';
        unset($_SESSION['error'][$key]);
    }
}

/* Set informational message in the session.
 */
function set_info_msg($key, $msg) {
    $_SESSION['info'][$key] = $msg;
}

/* Print informational message (if any) and unsets it.
 */
function emit_info_msg($key) {
    if (isset($_SESSION['info'][$key])) {
        print '<div class="info_msg"><p>' . $_SESSION['info'][$key] . '</p></div>';
        unset($_SESSION['info'][$key]);
    }
}


/* Returns true if user has JS enabled (tested for by the presence of a
 * cookie which we have set using JS).
 */
function use_js() {
	$result = false;
	
	if(isset($_COOKIE['use_js']) && $_COOKIE['use_js']) {
		$result = true;
	}
	return $result;
}

/* Returns true if the current user is logged in as the specified type
 * ('parent' or 'admin')
 */
function logged_in($user_type) {
	if($user_type == 'parent') {
		return isset($_SESSION['parent']) && 
				$_SESSION['parent']['logged_in'];
	}
	
	if($user_type == 'admin') {
		return isset($_SESSION['admin']) && 
				$_SESSION['admin']['logged_in'];
	}

	return false;
}

/* Bump a logged in user to their home page (from the login pages)
 */
function divert_from_login() {

    global $urls;

    if (logged_in('parent')) {
        header('Location: ' . $urls['parent']['home']);
    } else if (logged_in('admin')) {
        header('Location: ' . $urls['admin']['home']);
    }

}

/* Returns true if the parent of the given id has an entry in the settings
 * table.
 */
function parent_has_settings($pid) {
    return !isnull(get_parent_prefs($pid));
}

/* Redirects to the login page for user type $type if they are not currently
 * logged in.
 */
function require_login($type) {

    global $urls;

    if (!logged_in($type)) {
        header('Location: ' . $urls[$type]['login']);
    }
}

/* Loads the configuration file once into a global variable. */
function load_conf($reload = false) {
	if(!isset($GLOBALS['web_conf']) || $reload) {
		$GLOBALS['web_conf']= parse_ini_file('./conf.ini', true);
	}
}

function is_time($text) {
	return preg_match("/^\d\d:\d\d$/", $text);
}

function get_time($text) {
	return strtotime($text);
}

function is_date($text) {
	return preg_match(
			"/^(0[1-9]|[12][0-9]|3[01])[\/-](0[1-9]|1[012])[\/-](19|20)\d\d$/", $text);
}

function get_date($text) {
	$separators = array("/", ".", " ", "-");
	$str_date = str_replace($separators, '.', $text);
	return strtotime($str_date);
}

function timestamp_to_str($timestamp) {
	return strftime('%F %T', $timestamp);
}

function open_session($rsid) {
	$result = open_reg_session($rsid);
		
	// send out e-mails
	send_open_session_emails();
	
	return $result;
}

function close_session($rsid) {
	$result = close_reg_session($rsid);
	
	// send out e-mails
	send_closed_session_emails();
	
	return $result;
}

function archive_session($rsid) {
	$result = update_session_status($rsid, $const['reg_status']['archived']);
	return $result;
}

function delete_session ($rsid) {
	$result = delete_reg_session($rsid);
	return $result;
}

function schedule_session($rsid) {
	$result = true;
	
	load_conf();
	$dbconf= $GLOBALS['web_conf']['dbconf'];

	delete_schedules($rsid);

	$days = get_session_parent_days($rsid);
	foreach($days as $day) {
		$command = "java -jar " . realpath("./java/juliet.jar") . ' ' .
				"-db tcp://" . $dbconf['db_host'] . ':' . $dbconf['db_tcp_port'] . '/ptadb ' .
				"-dbd " . $day['did'] . ' ' .
				"-dbc " . $day['day'] . ' ' .
				"-dbs ptaweb " .
				"-u " . $dbconf['db_user'] . ' ' .
				"-p " . $dbconf['db_pass'];
				
		// escape the whole command, if we have to
		$command = escapeshellcmd($command);
		
		$output = '';
		$ret_code = '';
		exec($command, $output, $ret_code);
	}
	
	send_scheduled_session_emails();
	
	return $result;
}

function send_open_session_emails() {
	$parents = get_parents();
	$headers  = 'MIME-Version: 1.0' . "\r\n";
	$headers .= 'Content-type: text/plain; charset=iso-8859-1' . "\r\n";
	
	load_conf();
	
	$template = $GLOBALS['web_conf']['open_session_email'];
	$subj = $template['subject'];
	$msg = $template['text'];
	
	$website = $GLOBALS['web_conf']['website']['web_root'];
	$msg = str_replace('##WEBSITE##', $website, $msg);
	
	$open_sess = get_open_session();
	$sess = $open_sess[0];
	$msg = str_replace('##DATE##',
						date('d/M/Y H:i A',strtotime($sess['endts'])),
						$msg);
	
	foreach($parents as $parent) {
		if(!empty($parent['email'])) {
			$new_msg = str_replace('##PARENT##',
									$parent['fname'] . ' ' . $parent['lname'],
									$msg);
			mail($parent['email'], $subj, $new_msg, $headers);
		}
	}
}

function send_closed_session_emails() {
	$parents = get_parents();
	$headers  = 'MIME-Version: 1.0' . "\r\n";
	$headers .= 'Content-type: text/plain; charset=iso-8859-1' . "\r\n";
	
	load_conf();
	
	$template = $GLOBALS['web_conf']['close_session_email'];
	$subj = $template['subject'];
	$msg = $template['text'];
	
	$website = $GLOBALS['web_conf']['website']['web_root'];
	$msg = str_replace('##WEBSITE##', $website, $msg);
	
	foreach($parents as $parent) {
		if(!empty($parent['email'])) {
			$new_msg = str_replace('##PARENT##',
									$parent['fname'] . ' ' . $parent['lname'],
									$msg);
			mail($parent['email'], $subj, $new_msg, $headers);
		}
	}
}

function send_scheduled_session_emails() {
	$parents = get_parents();
	$headers  = 'MIME-Version: 1.0' . "\r\n";
	$headers .= 'Content-type: text/plain; charset=iso-8859-1' . "\r\n";
	
	load_conf();
	
	$template = $GLOBALS['web_conf']['scheduled_session_email'];
	$subj = $template['subject'];
	$msg = $template['text'];
	
	$website = $GLOBALS['web_conf']['website']['web_root'];
	$msg = str_replace('##WEBSITE##', $website, $msg);
	
	foreach($parents as $parent) {
		if(!empty($parent['email'])) {
			$new_msg = str_replace('##PARENT##',
									$parent['fname'] . ' ' . $parent['lname'],
									$msg);
			mail($parent['email'], $subj, $new_msg, $headers);
		}
	}
}

function get_aft_appts($sid) {
	$result = array();
	$teachers = get_teachers();
	$num_aft_appts = get_num_aft_appts($sid);
	$num = $num_aft_appts['num'];
	$i = 0;
	$appts = query_aft_appts($sid);
	
	foreach($teachers as $teacher) {
		for($i = 0; $i <= $num; $i++) {
			$result[$teacher['tid']][$i] = null;
		}
	}
	
	foreach($appts as $ap) {
			$result[$ap['tid']][$ap['slot']] = $ap['pid'];
	}

	return $result;
}

function get_eve_appts($sid) {
	$result = array();
	$teachers = get_teachers();
	$num_eve_appts = get_num_eve_appts($sid);
	$num = $num_eve_appts['num'];
	$i = 0;
	$appts = query_eve_appts($sid);
	
	foreach($teachers as $teacher) {
		for($i = 0; $i <= $num; $i++) {
			$result[$teacher['tid']][$i] = null;
		}
	}
	
	foreach($appts as $ap) {
		$result[$ap['tid']][$ap['slot']] = $ap['pid'];
	}
	
	return $result;
}

function get_aft_appts_parent($sid, $pid) {
	$result = array();
	
	$num_aft_appts = get_num_aft_appts($sid);
	$num = $num_aft_appts['num'];
	$i = 0;
	$appts = query_aft_appts_parent($sid, $pid);
	
	foreach($appts as $ap) {
		for($i = 0; $i <= $num; $i++) {
			$result[$ap['tid']][$i] = null;
		}
	}
	
	foreach($appts as $ap) {
		$result[$ap['tid']][$ap['slot']] = $ap['pid'];
	}
	
	return $result;
}

function get_eve_appts_parent($sid, $pid) {
	$result = array();
	
	$num_eve_appts = get_num_eve_appts($sid);
	$num = $num_eve_appts['num'];
	$i = 0;
	$appts = query_eve_appts_parent($sid, $pid);
	
	foreach($appts as $ap) {
		for($i = 0; $i <= $num; $i++) {
			$result[$ap['tid']][$i] = null;
		}
	}
	
	foreach($appts as $ap) {
		$result[$ap['tid']][$ap['slot']] = $ap['pid'];
	}
	
	return $result;
}

function get_tocall_list($rsid) {
	$i = 0;
	$result = array();
	
	$m_appts = get_unsched_requests($rsid);
	
	$num = count($m_appts);
	
	foreach($m_appts as $ap) {
			$result[$ap['tid']][] = $ap['pid'];
	}
	
	$max = 0;
	
	foreach($result as $item) {
		if(count($item) > $max) {
			$max = count($item);
		}
	}	
	
	foreach($result as $tid => $item) {
		for($i=0; $i < $max; $i++) {
			if(!isset($result[$tid][$i])) {
				$result[$tid][$i] = ' ';
			}
		}
	}

	return $result;
}


function is_windows() {
	return (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN');
}


?>
