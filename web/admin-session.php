<?php

	session_start();

	include_once("functions.php");
	include_once("db_conn.php");  
	
    require_login('admin');

	global $urls;

	// handle requests if any
	if($_POST['do_not_create']) {
		header('Location: ' . $urls['admin']['home']);
	}

	if($_POST['cancel_edit']) {
		// do nothing, for now
		// maybe set a message, but not necessary
	}

	if($_POST['delete']) {
		$reg_session = get_curr_reg_session();
		if($reg_session) {
			$success = delete_session($reg_session['rsid']);
			set_info_msg('sess_man',
							'Session deleted successfully.');
		}
		if(!$reg_session || !$success) {
			set_error_msg('sess_man_error', "The session could not be deleted.");
		}
	}
	
	if($_POST['open']) {
		$ret_session = get_new_session();
		$reg_session = $ret_session[0];
		if($reg_session) {
			$success = open_session($reg_session['rsid']);
			set_info_msg('sess_man',
							'Session opened successfully.');
		}
		if(!$reg_session || !$success) {
			set_error_msg('sess_man_error',"The session could not be opened.");
		}
	}

	if($_POST['close']) {
		$ret_session = get_open_session();
		$reg_session = $ret_session[0];
		if($reg_session) {
			$success = close_session($reg_session['rsid']);
			set_info_msg('sess_man',
							'Session closed successfully.');
		}
		if(!$reg_session || !$success) {
			set_error_msg('sess_man_error',"The session could not be closed.");
		}
	}

	if($_POST['schedule']) {
		// run the scheduler
		// set the session as scheduled (or archived (4))
		$ret_session = get_closed_session();
		$reg_session = $ret_session[0];
		if($reg_session) {
			$t_rsid = $reg_session['rsid'];
			// make it run the scheduler
			
			
			$success = schedule_session($t_rsid);
			set_info_msg('sess_man',
							'Session scheduled successfully.');
	}
		if(!$reg_session || !$success) {
			set_error_msg('sess_man_error',"The session could not be scheduled.");
		}
	}

	if($_POST['create']) {
		html_header("Session Administration");
		html_banner("Session Administration");
		$fields = array('sess_start_date' => 'dd/mm/yyyy',
						'sess_start_time' => 'hh:mm',
						'sess_end_date' => 'dd/mm/yyyy',
						'sess_end_time' => 'hh:mm',
						'sess_day1' => 'dd/mm/yyyy',
						'sess_day2' => 'dd/mm/yyyy',
						'sess_tb1_start' => 'hh:mm',
						'sess_tb1_end' => 'hh:mm',
						'sess_tb2_start' => 'hh:mm',
						'sess_tb2_end' => 'hh:mm',
						'sess_meet_len' => '');
		show_session_form('create', $fields);
		html_footer();
		return;
	}

	if($_POST['edit']) {
		$fields = array();
		$ret_session = get_new_session();
		$reg_session = $ret_session[0];
		$days = get_session_parent_days($reg_session['rsid']);
		
		$startts = strtotime($reg_session['startts']);
		$endts = strtotime($reg_session['endts']);

		$fields['sess_start_date'] = date('d/m/Y', $startts) ;
		$fields['sess_start_time'] = date('H:i', $startts) ;
		$fields['sess_end_date'] = date('d/m/Y', $endts) ;
		$fields['sess_end_time'] = date('H:i', $endts) ;
		$fields['sess_day1'] = date('d/m/Y',strtotime($days[0]['day']));
		$fields['sess_day2'] = date('d/m/Y', strtotime($days[1]['day']));
		$fields['sess_tb1_start'] = substr($reg_session['aft_start'], 0, 5);
		$fields['sess_tb1_end'] = substr($reg_session['aft_end'], 0, 5);
		$fields['sess_tb2_start'] = substr($reg_session['eve_start'], 0, 5);
		$fields['sess_tb2_end'] = substr($reg_session['eve_end'], 0, 5);
		$fields['sess_meet_len'] = $reg_session['meet_len'];
		html_banner("Session Administration");
		html_header("Session Administration");
		show_session_form('edit', $fields);
		html_footer();
		return;
	}

	if($_POST['update_sess']) {
		$fields = get_form_data();
		
		$valid = validate_session_form($fields);
		if($valid) {
			$startts = strftime('%F %T', get_date($fields['sess_start_date'] .
								' ' . $fields['sess_start_time'] . ':00'));
			$endts = strftime('%F %T', get_date($fields['sess_end_date'] . 
								' ' . $fields['sess_end_time'] . ':00'));
			$tb1_start = $fields['sess_tb1_start'] . ':00';
			$tb1_end = $fields['sess_tb1_end'] . ':00';
			$tb2_start = $fields['sess_tb2_start'] . ':00';
			$tb2_end = $fields['sess_tb2_end'] . ':00';
			
			$ret_session = get_new_session();
			$reg_session = $ret_session[0];
			$t_rsid = $reg_session['rsid'];
			
			$success = update_reg_session($t_rsid, $startts, $endts,
											$tb1_start, $tb1_end,
											$tb2_start, $tb2_end,
											intval($fields['sess_meet_len']));
											
			if($success) {
				$day1 = strftime('%F', get_date($fields['sess_day1']));
				$day2 = strftime('%F', get_date($fields['sess_day2']));
				delete_sess_dates($t_rsid);
				$success = $success && add_date($t_rsid, $day1);
				$success = $success && add_date($t_rsid, $day2); 
			}

			if($success) { 
				set_info_msg('add_session_status',
							'Session updated successfully.');
				header('Location: ' . 'admin-session.php');
			} else {
				set_info_msg('add_session_status',
							'The session could not be updated.');
			}
		} else {
			set_error_msg('add_session_error',
							'Please correct the errors below.');
		}
			
	
		html_header("Session Administration");
		html_banner("Session Administration");
		show_session_form('edit', $fields);
		html_footer();
		return;
	}


	if($_POST['create_sess']) {
		
		$fields = get_form_data();
		
		$valid = validate_session_form($fields);
		
		if($valid) {
			$startts = strftime('%F %T', get_date($fields['sess_start_date'] .
								' ' . $fields['sess_start_time'] . ':00'));
			$endts = strftime('%F %T', get_date($fields['sess_end_date'] . 
								' ' . $fields['sess_end_time'] . ':00'));

			$tb1_start = $fields['sess_tb1_start'] . ':00';
			$tb1_end = $fields['sess_tb1_end'] . ':00';
			$tb2_start = $fields['sess_tb2_start'] . ':00';
			$tb2_end = $fields['sess_tb2_end'] . ':00';

			$success = add_reg_session($startts, $endts,
											$tb1_start, $tb1_end,
											$tb2_start, $tb2_end,
											intval($fields['sess_meet_len']));
			
			if($success) {
				$ret_session = get_new_session();
				$reg_session = $ret_session[0];
				$t_rsid = $reg_session['rsid'];
				
				$day1 = strftime('%F', get_date($fields['sess_day1']));
				$day2 = strftime('%F', get_date($fields['sess_day2']));
				
				$success = $success && add_date($t_rsid, $day1);
				$success = $success && add_date($t_rsid, $day2); 
			}

			if($success) { 
				set_info_msg('add_session_status',
							'Session created successfully.');
				header('Location: ' . 'admin-session.php');
			} else {
				set_info_msg('add_session_status',
							'The session could not be created.');
			}
		} else {
			set_error_msg('add_session_error',
							'Please correct the errors below.');
		}
		
		html_header("Session Administration");
		html_banner("Session Administration");
		show_session_form('create', $fields);
		html_footer();
		return;
	}

	// handle displaying the information
	$new_session = get_new_session();
	$open_session = get_open_session();
	$closed_session = get_closed_session();

	if($new_session) {
		$session = $new_session[0];
	} else if($open_session) {
		$session = $open_session[0];
	} else if($closed_session) {
		$session = $closed_session[0];
	} else {
		$session = NULL;
	}

	if($session) {
		$rsid = $session['rsid'];
		$status = $session['status'];
		$startts = $session['startts'];
		$endts = $session['endts'];
		$aft_start = $session['aft_start'];
		$aft_end = $session['aft_end'];
		$eve_start = $session['eve_start'];
		$eve_end = $session['eve_end'];
		$meet_len = $session['meet_len'];
		$days = get_session_parent_days($session['rsid']);
	} else {
		$status = 0; // no session
	}
	

	html_header("Session Administration");
	html_banner("Session Administration");

	emit_info_msg('sess_man');

	if($status == 1) {
		
		echo '<h2>New Registration Session</h2>';
		echo '<p>The session is accepting requests from ' .
				date('d-M-Y H:i A',strtotime($startts)) .
				' until ' . date('d-M-Y H:i A',strtotime($endts)) .'</p>';
		echo '<p>The available interview days are: <ul>';
	    foreach ($days as $d) {
			print '<li>' . date('d-M-Y',strtotime($d['day'])) . '</li>';
   		 }
		print '</ul>'; 
		echo '<p>For the afternoon block interviews start at '.  
				date('H:i A',strtotime($aft_start)) . 
				' and end at ' . date('H:i A',strtotime($aft_end)) . '.</p>';
		echo '<p>For the evening block interviews start at '.  
				date('H:i A',strtotime($eve_start)) . 
				' and end at ' . date('H:i A',strtotime($eve_end)) . '.</p>';
		echo '<p>The interview length is ' . $meet_len . ' minutes.</p>';?>
		<form id="new_session_form" method="POST">
			<div class="form-row">
    		    <input type="submit" name="edit" value="Edit Session" />
    		    <input type="submit" name="open" value="Open Session" />
		        <input type="submit" name="delete" value="Delete Session" />
		    </div>
		</form>
<?
	} else if($status == 2) {
		
		echo '<h2>Open Registration Session</h2>';
        echo '<p>The session is accepting requests from ' . 
        		date('d-M-Y H:i A',strtotime($startts)) .
                ' until ' . date('d-M-Y H:i A',strtotime($endts)) .'</p>';
        echo '<p>The available interview days are: <ul>';
        foreach ($days as $d) {
            print '<li>' . date('d-M-Y',strtotime($d['day'])) . '</li>';
         }
        print '</ul>'; 
        echo '<p>For the afternoon block interviews start at '.
        		date('H:i A',strtotime($aft_start)) . 
                ' and end at ' . date('H:i A',strtotime($aft_end)) . '.</p>';
        echo '<p>For the evening block interviews start at '. 
        		date('H:i A',strtotime($eve_start)) .  
                ' and end at ' . date('H:i A',strtotime($eve_end)) . '.</p>';
        echo '<p>The interview length is ' . $meet_len . ' minutes.</p>';?>
        <form id="open_session_form" method="POST">
            <div class="form-row">
                <input type="submit" name="close" value="Close Session" />
                <input type="submit" name="delete" value="Delete Session" />
            </div>
        </form>

<?
	} else if($status == 3) {
		echo '<h2>Closed Registration Session</h2>';
        echo '<p>The session stopped accepting requests after ' .
        		date('d-M-Y H:i A',strtotime($endts)) . '</p>';
        echo '<p>The interview days are: <ul>';
        foreach ($days as $d) {
            print '<li>' . date('d-M-Y',strtotime($d['day'])) . '</li>';
         }
        print '</ul>'; 
        echo '<p>For the afternoon block interviews start at '. 
        		date('H:i A',strtotime($aft_start)) . 
                ' and end at ' . date('H:i A',strtotime($aft_end)) . '.</p>';
        echo '<p>For the evening block interviews start at '.  
        		date('H:i A',strtotime($eve_start)) .  
                ' and end at ' . date('H:i A',strtotime($eve_end)) . '.</p>';
        echo '<p>The interview length is ' . $meet_len . ' minutes.</p>';?>
        <form id="closed_session_form" method="POST">
            <div class="form-row">
                <input type="submit" name="schedule" value="Create Schedule" />
                <input type="submit" name="delete" value="Delete Session" />
            </div>
        </form>
<?
	} else { 
		// offer to create a session ?>
		<h3>Current Session</h3>
		<p>There are no registration sessions at this time.</p>
		<p>Would you like to create a registration session?</p>
		<form id="create_session_form" method="POST">
			<div class="form-row">
    		    <input type="submit" name="create" value="Create Session" />
		        <input type="submit" name="do_not_create" value="No" />
		    </div>
		</form>
<?php
	} 

function show_session_form($mode, $fields) { ?>
	<form id="add_session_form" method="POST">
	
<?		if($mode == 'create') { ?>
		<h3>Create Session</h3>
<?		} else { ?>
		<h3>Edit Session</h3>
<?		}  
		
		emit_info_msg('add_session_status');
		emit_error_msg('add_session_error');
?>

		<h4>Session Start</h4>
		<div class="form-row">
            <label for="sess_start_date" >Date:</label>
            <?php emit_error_msg('sess_start_date'); ?>
            <input type="text" name="sess_start_date" 
					value="<?=$fields['sess_start_date']?>"/>
		</div>
		<div class="form-row">
            <label for="sess_start_time" >Time:</label>
			<?php emit_error_msg('sess_start_time'); ?>
            <input type="text" name="sess_start_time"  
					value="<?=$fields['sess_start_time']?>"/>
		</div>

		<h4>Session End</h4>
		<div class="form-row">
        	<label for="sess_end_date" >Date:</label>
			<? emit_error_msg('sess_end_date');?>
            <input type="text" name="sess_end_date"  
					value="<?=$fields['sess_end_date']?>"/>
       	</div>
		<div class="form-row">
            <label for="sess_end_time" >Time:</label>
			<? emit_error_msg('sess_end_time');?>
            <input type="text" name="sess_end_time"  
					value="<?=$fields['sess_end_time']?>"/>
		</div>

		<h4>Interview Days</h4>
		<div class="form-row">
        	<label for="sess_day1">Day 1:</label>
			<? emit_error_msg('sess_day1');?>
            <input type="text" name="sess_day1"  
					value="<?=$fields['sess_day1']?>"/>
        	</div>
		<div class="form-row">
            <label for="sess_day2">Day 2:</label>
			<? emit_error_msg('sess_day2');?>
            <input type="text" name="sess_day2"  
					value="<?=$fields['sess_day2']?>"/>
		</div>

		<h4>Interview Times</h4>
            <h5>Time Block 1:</h5>
			<div class="form-row">
			    <label for="sess_tb1_start">Start:</label>
                <? emit_error_msg('sess_tb1_start');?>
                <input type="text" name="sess_tb1_start"  
					value="<?=$fields['sess_tb1_start']?>"/>
            </div>
			<div class="form-row">
				<label for="sess_tb1_end">End:</label>
                <? emit_error_msg('sess_tb1_end');?>
                <input type="text" name="sess_tb1_end"  
					value="<?=$fields['sess_tb1_end']?>"/>
			</div>

            <h5>Time Block 2:</h5>
			<div class="form-row">
				<label for="sess_tb2_start">Start:</label>
                <? emit_error_msg('sess_tb2_start'); ?>
                <input type="text" name="sess_tb2_start"  
					value="<?=$fields['sess_tb2_start']?>"/>
                </div>
			<div class="form-row">
				<label for="sess_tb2_end">End:</label>
                <? emit_error_msg('sess_tb2_end'); ?>
                <input type="text" name="sess_tb2_end"  
					value="<?=$fields['sess_tb2_end']?>"/>
			</div>

		<div class="form-row">
			<label for="sess_meet_len" >Meeting Length (in minutes)</label>
			<? emit_error_msg('sess_meet_len'); ?>
            <input type="text" name="sess_meet_len"  
					value="<?=$fields['sess_meet_len']?>"/>
		</div>
		<div class="form-row">
			<? emit_error_msg('add_session_status'); ?>
		</div>



		<div class="form-row">
<?			if($mode == 'create') { ?>
				<input type="submit" id="create_sess" name="create_sess"
					value="Create Session" />
<?			} else { ?>
				<input type="submit" id="update_sess" name="update_sess"
					value="Update Session" />
<?			} ?>
				<input type="submit" id="cancel_edit" name="cancel_edit"
					value="Cancel Editing" />
		</div>
</form>
<?php } // end of show_session_form() 

function get_form_data() {
	$result = array();
	$result['sess_start_date'] = trim($_POST['sess_start_date']);
	
	$result['sess_start_time'] = trim($_POST['sess_start_time']);
	
	$result['sess_end_date'] = trim($_POST['sess_end_date']);

	$result['sess_end_time'] = trim($_POST['sess_end_time']);

	$result['sess_day1'] = trim($_POST['sess_day1']);
	
	$result['sess_day2'] = trim($_POST['sess_day2']);

	$result['sess_tb1_start'] = trim($_POST['sess_tb1_start']);

	$result['sess_tb1_end'] = trim($_POST['sess_tb1_end']);

	$result['sess_tb2_start'] = trim($_POST['sess_tb2_start']);

	$result['sess_tb2_end'] = trim($_POST['sess_tb2_end']);

	$result['sess_meet_len'] = trim($_POST['sess_meet_len']);
	return $result;

}

function validate_session_form($info) {
	$result = true;
	// sess_start_date
	if(empty($info['sess_start_date'])) {
		set_error_msg('sess_start_date', "* Required");
		$result = false;
	} elseif(!is_date($info['sess_start_date'])) {
		set_error_msg('sess_start_date', "Valid date required (dd/mm/yyyy)");
		$result = false;
	}

	// sess_start_time
	if(empty($info['sess_start_time'])) {
		set_error_msg('sess_start_time', "* Required");
		$result = false;
	} elseif(!is_time($info['sess_start_time'])) {
		set_error_msg('sess_start_time', "Valid time required (hh:mm)");
		$result = false;
	}

	// sess_end_date
	if(empty($info['sess_end_date'])) {
		set_error_msg('sess_end_date', "* Required");
		$result = false;
	} elseif(!is_date($info['sess_end_date'])) {
		set_error_msg('sess_end_date', "Valid date required (dd/mm/yyyy)");
		$result = false;
	}

	// sess_end_time
	if(empty($info['sess_end_time'])) {
		set_error_msg('sess_end_time', "* Required");
		$result = false;
	} elseif(!is_time($info['sess_end_time'])) {
		set_error_msg('sess_end_time', "Valid time required (hh:mm)");
		$result = false;
	}

	// sess_day1
	if(empty($info['sess_day1'])) {
		set_error_msg('sess_day1', "* Required");
		$result = false;
	} elseif(!is_date($info['sess_day1'])) {
		set_error_msg('sess_day1', "Valid date required (dd/mm/yyyy)");
		$result = false;
	}

	// sess_day2
	if(empty($info['sess_day2'])) {
		set_error_msg('sess_day2', "* Required");
		$result = false;
	} elseif(!is_date($info['sess_day2'])) {
		set_error_msg('sess_day2', "Valid date required (dd/mm/yyyy)");
		$result = false;
	}
	
	// sess_tb1_start
	if(empty($info['sess_tb1_start'])) {
		set_error_msg('sess_tb1_start', "* Required");
		$result = false;
	} elseif(!is_time($info['sess_tb1_start'])) {
		set_error_msg('sess_tb1_start', "Valid time required (hh:mm)");
		$result = false;
	}

	// sess_tb1_end
	if(empty($info['sess_tb1_end'])) {
		set_error_msg('sess_tb1_end', "* Required");
		$result = false;
	} elseif(!is_time($info['sess_tb1_end'])) {
		set_error_msg('sess_tb1_end', "Valid time required (hh:mm)");
		$result = false;
	}

	// sess_tb2_start
	if(empty($info['sess_tb2_start'])) {
		set_error_msg('sess_tb2_start', "* Required");
		$result = false;
	} elseif(!is_time($info['sess_tb2_start'])) {
		set_error_msg('sess_tb2_start', "Valid time required (hh:mm)");
		$result = false;
	}

	// sess_tb2_end
	if(empty($info['sess_tb2_end'])) {
		set_error_msg('sess_tb2_end', "* Required");
		$result = false;
	} elseif(!is_time($info['sess_tb2_end'])) {
		set_error_msg('sess_tb2_end', "Valid time required (hh:mm)");
		$result = false;
	}

	// sess_meet_len
	if(empty($info['sess_meet_len'])) {
		set_error_msg('sess_meet_len', "* Required");
		$result = false;
	} elseif(!is_numeric($info['sess_meet_len']) ||
				intval($info['sess_meet_len']) <= 0) {
		set_error_msg('sess_meet_len', "Valid number of minutes required.");
		$result = false;
	}

	return $result;
}
?>

<?php html_footer(); ?>
