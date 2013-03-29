<?php

	session_start();

	include_once("functions.php"); 

    require_login('admin');	
	
	html_header("To-Call List");
	html_banner("To-Call List");
	
	function get_parent_by_id($parents, $id) {
        foreach ($parents as $parent) {
            if ($parent['pid'] == $id)
                return $parent;
        }
        return null;
    }
    
    function get_teacher_by_id($teachers, $id) {
        foreach ($teachers as $teacher) {
            if ($teacher['tid'] == $id)
                return $teacher;
        }
        return null;
    }
	
    function do_table($raw) {
        $teachers = get_teachers();
        $parents = get_parents();
        $header = "<th>Teacher</th>";
        $header_done = false;
        $output = "";
        $i = 0;

        $time = strtotime($start_time);
        foreach ($raw as $tid => $pids) {
        	$teacher = get_teacher_by_id($teachers, $tid);
        	
            $output .= "<tr><td class='teacher'>" . $teacher['fname'] . " " . $teacher['lname'] . " </td>";
            foreach($pids as $pid) {
				$parent = get_parent_by_id($parents, $pid);
                $output .= "<td>" . $parent['fname'] . " " .  $parent['lname'] . "</td>";
                if (!$header_done) {
                	$i++;
                    $header .= "<th>Parent $i</th>";;
                }
            }
            $header_done = true;
            $output .= "</tr>\n";
        }

        return "<tr>$header</tr>$output";
    }

	$closed_session = get_closed_session();
    $schedules = get_schedules();
    
    if ($closed_session && $schedules) {
    	$session = $closed_session[0];
    	$rsid = $session['rsid'];
    	
    	$list = get_tocall_list($rsid);
    
		$table = do_table($list);
		if(!$table) {
			$table = "<h2>There are no parents on the to-call list.</h2>";
		}
		
    } else {
		$table = "<h2>There are no closed sessions and thus no parents on the to-call list.</h2>";
    }

    emit_info_msg('tocall');
    emit_error_msg('tocall');
?>

<? if($table) { ?>
<h3>Parents To Call</h3>
<br/>
<table class="timetable" id="aft">
    <? print $table ?>
</table>
<? } ?>

<?php html_footer(); ?>
