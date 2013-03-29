<?php

	session_start();
	include_once("functions.php"); 

    require_login('parent');	
	
	html_header("Schedule");
	html_banner("Schedule"); 	
    $schedules = get_schedules();
    $times = get_sched_times($_GET['sid']);
    
    function do_table($raw, $start_time, $meet_len) {
        $output = "";

        $time = strtotime($start_time);

		foreach($raw as $tid => $appts) {;
			$s = intval($appt['slot']);
			$teacher = get_teacher($tid);
			foreach($appts as $index => $ap) {
				if($ap) {
					$output .= '<tr><td>' . $teacher['fname'] . ' ' . $teacher['lname'] . '</td><td>' . date('H:i A', $time + $meet_len * ($index -1) * 60) . '</td>'; 
				}
			}
		}

        return "$output";
    }

    if (isset($_GET['sid'])) {
        // Viewing a specific schedule
        $sid = $_GET['sid'];
        $aft = get_aft_appts_parent($sid, $_SESSION['parent']['pid']);
        $eve = get_eve_appts_parent($sid, $_SESSION['parent']['pid']);
		
        $aft_table = do_table($aft, $times['aft_start'], $times['meet_len']);
        $eve_table = do_table($eve, $times['eve_start'], $times['meet_len']);
		if(!empty($aft_table)) {
			$aft_table = '<tr><th>Teacher</th><th>Appointment Time</th></tr>' .
							$aft_table;
		} else {
			$aft_table = null;
		}
		
		if(!empty($eve_table)) {
			$eve_table = '<tr><th>Teacher</th><th>Appointment Time</th></tr>' .
							$eve_table;
		} else {
			$eve_table = null;
		}

    } else {
        // Viewing list of schedules
		$aft_table = null;
		$eve_table = null;
    }

    emit_info_msg('schedule');
    emit_error_msg('schedule');
?>

<form id="select_schedule" method="get">
    <h3>Select a schedule</h3>
    <div class="form-row">
        <select name="sid">
            <option value="null">&lt;schedules&gt;</option>
<?
    foreach($schedules as $s) {
        printf(
'            <option value="%u">%s</option>
', $s['sid'], $s['day']);
    }
?>
        </select>
        <input type="submit" name="view" value="View selected schedule" />
    </div>
</form>

<? if($aft_table) { ?>
<h3>Afternoon appointments</h3>
<table class="timetable" id="aft">
    <? print $aft_table ?>
</table>
<? }

	if($eve_table) { ?>
<h3>Evening appointments</h3>
<table class="timetable" id="eve">
    <? print $eve_table ?>
</table>
<? } ?>

<?php html_footer(); ?>
