<?php

	session_start();

	include_once("functions.php"); 
    include_once("db_conn.php");

    require_login('admin');	

	html_header("Admin: Home");
	html_banner("Admin: Home"); 


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

	if($status == 1) {
		
		echo '<h2>The Current Registration Session is New</h2>';
		echo '<p>The session is accepting requests from ' .
				date('d-M-Y H:i A',strtotime($startts)) .
				' until ' . date('d/M/Y H:i A',strtotime($endts)) .'</p>';
		echo '<p>The available interview days are: <ul>';
	    foreach ($days as $d) {
			print '<li>' . date('d-M-Y',strtotime($d['day'])) . '</li>';
   		 }
		print '</ul>';
		echo '<p>The interview length is ' . $meet_len . ' minutes.</p>';
		
	} else if($status == 2) {
		
		echo '<h2>The Current Registration Session is Open</h2>';
        echo '<p>The session is accepting requests from ' . 
        		date('d-M-Y H:i A',strtotime($startts)) .
                ' until ' . date('d-M-Y H:i A',strtotime($endts)) .'</p>';
        echo '<p>The available interview days are: <ul>';
        foreach ($days as $d) {
            print '<li>' . date('d-M-Y',strtotime($d['day'])) . '</li>';
         }
        print '</ul>'; 
        echo '<p>The interview length is ' . $meet_len . ' minutes.</p>';
        
	} else if($status == 3) {
		echo '<h2>The Current Registration Session is Closed</h2>';
        echo '<p>The session stopped accepting requests after ' .
        		date('d-M-Y H:i A',strtotime($endts)) . '</p>';
        echo '<p>The interview days are: <ul>';
        foreach ($days as $d) {
            print '<li>' . date('d-M-Y',strtotime($d['day'])) . '</li>';
         }
        print '</ul>'; 
        echo '<p>The interview length is ' . $meet_len . ' minutes.</p>';
	} else {
		echo '<h2>The are no Registration Sessions at this time</h2>';
	}

	html_footer(); ?>
