<?php

	session_start();

	include_once("functions.php"); 

    require_login('admin');	
	
	html_header("Schedules");
	html_banner("Schedules");

    $schedules = get_schedules();
    $times = get_sched_times($_GET['sid']);
    
    function get_parent_by_id($parents, $id) {
        foreach ($parents as $parent) {
            if ($parent['pid'] == $id)
                return $parent;
        }
        return null;
    }

    function do_table($raw, $start_time, $meet_len) {
        $teachers = get_teachers();
        $parents = get_parents();
        $output = "";
        $header = "<th></th>";
        $header_done = false;

        $time = strtotime($start_time);
        foreach ($teachers as $t) {
            $appts = $raw[$t['tid']];

            $output .= "<tr><td class='teacher'>" . $t['fname'] . " " . $t['lname'] . " </td>";
            foreach($appts as $pid) {
                $parent = get_parent_by_id($parents, $pid);
                $output .= "<td>" . $parent['fname'] . " " .  $parent['lname'] . "</td>";
                if (!$header_done) {
                    $header .= "<th>" . date('H:i', $time) . "</th>";
                    $time += $meet_len * 60;
                }
            }
            $header_done = true;
            $output .= "</tr>\n";
        }

        return "<tr>$header</tr>$output";
    }

    if (isset($_GET['sid'])) {
        // Viewing a specific schedule
        $sid = $_GET['sid'];
        $aft = get_aft_appts($sid);
        $eve = get_eve_appts($sid);

        $aft_table = do_table($aft, $times['aft_start'], $times['meet_len']);
        $eve_table = do_table($eve, $times['eve_start'], $times['meet_len']);

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
'            <option value="%u">Schedule option for %s</option>
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
