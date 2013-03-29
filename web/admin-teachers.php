<?php

	session_start();

	include_once("functions.php"); 
    include_once("db_conn.php");
	
    require_login('admin');

    $add_visible = "none";
    $edit_visible = "none";
    $delete_visible = "none";
    $hr_visible = "none";

    if (isset($_POST['add'])) {

        if (isset($_POST['do_add'])) {
            // This was a form submission, change the DB
            if ( add_teacher($_POST['fname'], $_POST['lname']) ) {
                set_info_msg('admin-teacher', 'Successfully added teacher');
            } else {
                set_info_msg('admin-teacher', 'Failed to add teacher');
            }
        } else {
            // We're just loading the form.
            $add_visible = "inline";
            $hr_visible = "block";
        }

    } else if (isset($_POST['tid']) && $_POST['tid'] != "null") {

        if (isset($_POST['delete'])) {
            
            if (isset($_POST['do_delete'])) {
                // This was a form submission, change the DB
                $ret = delete_teacher($_POST['tid']);
                if ( $ret ) {
                    set_info_msg('admin-teacher', 'Successfully deleted teacher id ' . $_POST['tid']);
                } else {
                    set_info_msg('admin-teacher', 'Failed to delete teacher id ' . $_POST['tid']);
                }
            } else {
                // We're just loading the form
                $tch = get_teacher($_POST['tid']);
                $delete_visible = "inline";
                $hr_visible = "block";
            }

        } else if (isset($_POST['edit'])) {

            if (isset($_POST['do_edit'])) {
                // This was a form submission, change the DB
                if ( update_teacher($_POST['tid'], $_POST['fname'], $_POST['lname']) ) {
                    set_info_msg('admin-teacher', 'Saved changes to teacher id ' . $_POST['tid']);
                } else {
                    set_info_msg('admin-teacher', 'Failed to update teacher id ' . $_POST['tid']);
                }
            } else {
                // We're just loading the form
                $tch = get_teacher($_POST['tid']);
                $edit_visible = "inline";
                $hr_visible = "block";
            }
        }

    }

    // Messages for cancelled actions
    if (isset($_POST['do_not_add'])) {
        set_info_msg('admin-teacher', 'Teacher was not added.');
    } else if (isset($_POST['do_not_delete'])) {
        set_info_msg('admin-teacher', 'Teacher was not deleted.');
    } else if (isset($_POST['do_not_edit'])) {
        set_info_msg('admin-teacher', 'Teacher was not modified.');
    }



// Page rendering starts...

	html_header("Administration: Teachers");
	html_banner("Manage Teachers");

    emit_info_msg('admin-teacher');
    emit_error_msg('db_exception');
?>


<form id="delete_teacher_form" method="POST" style="display: <?php print $delete_visible ?>" >
    <h3>Confirm deletion</h3>
    <input type="hidden" name="do_delete" />
    <input type="hidden" name="tid" id="tid" value="<? print $tch['tid'] ?>" />
    <p class="form-row">
        Are you sure that you want to delete
            <em><? print $tch['fname'] . ' ' . $tch['lname'] ?></em>?
    </p>
    <div class="form-row">
        <input type="submit" name="delete" value="Yes, delete this teacher" />
        <input type="submit" name="do_not_delete" value="No, keep this teacher" />
    </div>
</form>

<form id="edit_teacher_form" method="POST" style="display: <?php print $edit_visible ?>" >
    <h3>Editing teacher</h3>
    <input type="hidden" name="tid" id="tid" value="<? print $tch['tid'] ?>" />
    <input type="hidden" name="do_edit" />
    <div class="form-row">
        <label for="fname">First Name</label>
        <input type="text" name="fname" id="fname" value="<? print $tch['fname'] ?>" />
    </div>
    <div class="form-row">
        <label for="lname">Last Name</label>
        <input type="text" name="lname" id="lname" value="<? print $tch['lname'] ?>" />
    </div>
    <div class="form-row">
        <input type="submit" id="edit_teacher" name="edit" value="Save changes" />
        <input type="submit" name="do_not_edit" value="Discard changes" />
    </div>
</form>

<form id="add_teacher_form" method="POST" style="display: <?php print $add_visible ?>" >
    <h3>Add a new teacher</h3>
    <input type="hidden" name="do_add" />
    <div class="form-row">
        <label for="fname">First Name</label>
        <input type="text" name="fname" id="fname"  />
    </div>
    <div class="form-row">
        <label for="lname">Last Name</label>
        <input type="text" name="lname" id="lname"  />
    </div>
    <?php emit_error_msg('add_teacher_error'); ?>
    <div class="form-row">
        <input type="submit" id="add_teacher" name="add" value="Add Teacher" />
        <input type="submit" id="add_teacher" name="do_not_add" value="Discard Teacher" />
    </div>
</form>

<hr style="display: <?php print $hr_visible ?>" />

<div id="teacher_list">
    <form method="POST">
    <div class="form-row">
    <input type="submit" name="add" value="Create new teacher" />
    </div>
    <div class="form-row">
    <select name="tid">
        <option value="null">&lt;teachers&gt;</option>
<?php
    $teachers = get_teachers();

    foreach($teachers as $t) {
        printf("<option value=%s>%s, %s</option>\n",
            $t['tid'], $t['lname'], $t['fname']);
    }
    
    print '</select>
<input type="submit" name="edit" value="Edit selected teacher" />
<input type="submit" name="delete" value="Delete selected teacher" />
</div>';

?>
    </form>
</div>
<?php html_footer(); ?>
