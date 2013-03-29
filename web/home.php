<?php

	session_start();
	
	include_once("functions.php");
    include_once("db_conn.php");
    global $urls;
    global $const;
    global $urls;

    require_login('parent');

    // If the parent has no settings, redirect them to the settings page.
    $prefs = get_parent_prefs($_SESSION['parent']['pid']);
    if (is_null($prefs)) {
        set_info_msg('settings', 'You have not yet entered any preferences');
        header('Location: ' . $urls['parent']['settings']);
    }

    // Get parent RSID, DID
    $open_sessions = get_open_session();
    $closed_sessions = get_closed_session();
    if($open_sessions) { 
        $session = $open_sessions[0];
    } elseif($closed_sessions) { 
        $session = $closed_sessions[0];
        header('Location: ' . $urls['parent']['schedule']);
        return;
    }

	$rsid = $session['rsid']; 

    /** Form setup ***********************************************************/
     
    // Get all teachers
    $teachers = get_teachers();

    // Get all of this parent's requests
    $req = get_requests_by_parent($_SESSION['parent']['pid'], $rsid);

	$tb = $_SESSION['parent']['tb'];


    /** Form processing ******************************************************/

    if (isset($_POST['make_request'])) {
		$keep_going = true;

		$aft_tb = $_POST['aft_tb'];
		$eve_tb = $_POST['eve_tb'];

		if($aft_tb == 'aft' && $eve_tb == 'eve') {
			$tb = 0;
			$_SESSION['parent']['tb'] = 0;
			update_parent_tb($_SESSION['parent']['pid'], 0);
		} elseif ($eve_tb == 'eve') {
			$tb = 2;
			$_SESSION['parent']['tb'] = 2;
			update_parent_tb($_SESSION['parent']['pid'], 2);
		} elseif ($aft_tb == 'aft') {
			$tb = 1;
			$_SESSION['parent']['tb'] = 1;
			update_parent_tb($_SESSION['parent']['pid'], 1);
		} else {
			$keep_going = false;
			set_error_msg('home', "You must choose at least 1 session.");
		}

        $req_new = $_POST['requests'];
        if (is_array($req_new) && $keep_going) {
            $n_req = count($req_new);

            if ($n_req > $const['max_interviews']) {
                set_error_msg('home', "You may only choose "
                . $const['max_interviews'] . " teachers but you have selected "
                . $n_req . ". Please remove some and try again.");
            } else {
                $n_added = 0;


				$days = get_session_parent_days($rsid);
				$which_day = strcasecmp($_SESSION['parent']['lname'], 'm');
				if($which_day < 0) {
					$did = $days[0]['did'];
				} else {
					$did = $days[1]['did'];
				}

                // Elements left in $req need to be removed from the DB
                if (is_array($req))
                    foreach($req as $r)
                        delete_request($r['rid']);

                // Elements left in $req_new need to be added to the DB
                foreach($req_new as $r) {
                    $n_added += insert_request($_SESSION['parent']['pid'], $r, $did, $rsid);
                }

                set_info_msg('home', "Success. Added $n_added requests.");

                // Done with the new requests
                unset($req_new);

                // Reset $req to include all requests so the page renders properly
                $req = get_requests_by_parent($_SESSION['parent']['pid'], $rsid);

            }
        }
    }


    /** Page begins **********************************************************/

	html_header("Home"); 
	html_banner("Home");

    emit_info_msg('home');
    emit_error_msg('home');

?>


<h3>Submit interview request</h3>
<p>You may request interviews with up to <? print $const['max_interviews'] ?> 
teachers. You must also select interviews during a particular session. Make your selections below and press the &quot;Submit request&quot; button
at the bottom of the form. You may return and alter your choices at any time
until this registration session closes at <? echo date('H:i A', strtotime($session['endts'])); ?> on <? echo date('d-M-Y', strtotime($session['endts'])); ?>.</p>
<p>If you have filled out this form previously, the names of teachers who you
selected will appear <span id="demo_requested">highlighted</span> in the list
below.</p>

<form method="post">

<h4 style="clear:both">Select Session</h4>
<p>Please select the session during which you are available</p>
<div class="request_check <?
		if($tb == 0 || $tb == 1) {
			echo 'prev_requested';
		}
?>" style="width:50%">
	<input type="checkbox" name="aft_tb" value="aft" id="aft_check" <?
		if($tb == 0 || $tb == 1) {
			echo 'checked="true"';
		}
?>/>
	<label for="aft_check">Session 1 : <?
		echo 'from ' . date('H:i A', strtotime($session['aft_start'])) . 
			' until ' . date('H:i A', strtotime($session['aft_end'])); 
	?></label>
</div>
<div class="request_check <?
		if($tb == 0 || $tb == 2) {
			echo 'prev_requested';
		}
?>" style="width:50%">
	<input type="checkbox" name="eve_tb" value="eve" id="eve_check" <?
		if($tb == 0 || $tb == 2) {
			echo 'checked="true"';
		}
?>/>
	<label for="eve_check">Session 2 : <?
	echo 'from ' . date('H:i A', strtotime($session['eve_start'])) . 
		' until ' . date('H:i A', strtotime($session['eve_end'])); 

	?></label>
</div>

<h4 style="clear:both">Select teachers</h4>
<? foreach ($teachers as $t) { 

    // Find teachers that this parent has previously requested
    $class = 'request_check';
    if (is_array($req))
        foreach ($req as $r) {
            if ($r['tid'] == $t['tid']) {
                $class = $class . ' prev_requested';
                //unset($req[current($req)]); // So we've got fewer to search next time
                break; // Won't find two requests for the same teacher.
            }
        }

    // If there was an error, $req_new will still be populated so that we can
    // preserve the state of the checkboxes.
    $checked = '';
    if (is_array($req_new))
        foreach ($req_new as $r) {
            if ($r == $t['tid']) {
                $checked = ' checked="true"';
            }
        }
    
    print '    <div class="' . $class . '">
    <input type="checkbox" name="requests[]" value="'
    . $t['tid'] . '" id="t_' . $t['tid'] . '"'. $checked .' />
    <label for="t_' . $t['tid'] . '">' . $t['fname'] . ' ' . $t['lname'] . '</label>
    </div>';
}
?>


    <div class="form-row"><input type="submit" name="make_request" value="Submit request" /></div>
</form>
<?php
html_footer();
?>

