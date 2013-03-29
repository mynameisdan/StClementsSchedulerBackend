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

			$uname = trim($_POST['username']);
			$email = trim($_POST['email']);

			if(strlen($uname) > 40) {
				$add_visible = "inline";
	            $hr_visible = "block";
				set_error_msg('username',
					'Please choose a username of at most 40 characters.');
			} elseif($email != '' &&
							!filter_var($email, FILTER_VALIDATE_EMAIL)) {
				$add_visible = "inline";
	            $hr_visible = "block";
				set_error_msg('email',
					'The e-mail entered is invalid.');
			} elseif ( add_parent_user($_POST['fname'], $_POST['lname'],
                $uname, $_POST['passwd'], $email, 0) ) {
                set_info_msg('admin-parent', 'Successfully added parent');
            } else {
                set_info_msg('admin-parent', 'Failed to add parent');
            }
        } else {
            // We're just loading the form.
            $add_visible = "inline";
            $hr_visible = "block";
        }

    } else if (isset($_POST['pid']) && $_POST['pid'] != "null") {

        if (isset($_POST['delete'])) {
            
            if (isset($_POST['do_delete'])) {
                // This was a form submission, change the DB
                $ret = delete_parent_user($_POST['pid']);
                if ( $ret ) {
                    set_info_msg('admin-parent', 'Successfully deleted parent id ' . $_POST['pid']);
                } else {
                    set_info_msg('admin-parent', 'Failed to delete parent id ' . $_POST['pid']);
                }
            } else {
                // We're just loading the form
                $par = get_parent($_POST['pid']);
                $delete_visible = "inline";
                $hr_visible = "block";
            }

        } else if (isset($_POST['edit'])) {

            if (isset($_POST['do_edit'])) {
                // This was a form submission, change the DB

                // If the password form field was left blank, don't change the
                // password
                if ($_POST['passwd'] == "") {
                    $_POST['passwd'] = NULL;
                }

              	$uname = trim($_POST['username']);
				$email = trim($_POST['email']);

				if(strlen($uname) > 40) {
                	$par = get_parent($_POST['pid']);
	                $edit_visible = "inline";
    	            $hr_visible = "block";
					set_error_msg('username',
						'Please choose a username of at most 40 characters.');
				} elseif($email != '' &&
							!filter_var($email, FILTER_VALIDATE_EMAIL)) {
                	$par = get_parent($_POST['pid']);
	                $edit_visible = "inline";
    	            $hr_visible = "block";
					set_error_msg('email',
						'The e-mail entered is invalid.');
				} elseif ( update_parent($_POST['pid'], $_POST['fname'],
                    $_POST['lname'], $_POST['username'], $_POST['passwd'],
                    $_POST['email']) ) {

                    set_info_msg('admin-parent', 'Saved changes to parent id ' . $_POST['pid']);
                } else {
                    set_info_msg('admin-parent', 'Failed to update parent id ' . $_POST['pid']);
                }
            } else {
                // We're just loading the form
                $par = get_parent($_POST['pid']);
                $edit_visible = "inline";
                $hr_visible = "block";
            }
        }
    }

    // Messages for cancelled actions
    if (isset($_POST['do_not_add'])) {
        set_info_msg('admin-parent', 'Parent was not added.');
    } else if (isset($_POST['do_not_delete'])) {
        set_info_msg('admin-parent', 'Parent was not deleted.');
    } else if (isset($_POST['do_not_edit'])) {
        set_info_msg('admin-parent', 'Parent was not modified.');
    }

// Page rendering starts...

	html_header("Administration: Parent");
	html_banner("Manage Parents"); 

    emit_info_msg('admin-parent');
    emit_error_msg('db_exception');
?>


<form id="delete_parent_form" method="POST" style="display: <?php print $delete_visible ?>" >
    <h3>Confirm deletion</h3>
    <input type="hidden" name="do_delete" />
    <input type="hidden" name="pid" id="pid" value="<? print $par['pid'] ?>" />
    <p class="form-row">
        Are you sure that you want to delete
        <em><? print $par['fname'] . ' ' . $par['lname'] ?></em>?
    </p>
    <div class="form-row">
        <input type="submit" name="delete" value="Yes, delete this parent" />
        <input type="submit" name="do_not_delete" value="No, keep this parent" />
    </div>
</form>

<form id="edit_parent_form" method="POST" style="display: <?php print $edit_visible ?>" >
    <h3>Editing parent</h3>
    <input type="hidden" name="pid" id="pid" value="<? print $par['pid'] ?>" />
    <input type="hidden" name="do_edit" />
    <div class="form-row">
        <label for="fname">First Name</label>
        <input type="text" name="fname" id="fname" value="<? print $par['fname'] ?>" />
    </div>
    <div class="form-row">
        <label for="lname">Last Name</label>
        <input type="text" name="lname" id="lname" value="<? print $par['lname'] ?>" />
    </div>
    <div class="form-row">
    	<? emit_error_msg('username'); ?>
        <label for="username">Username</label>
        <input type="text" name="username"  value="<? print $par['username'] ?>" />
    </div>
    <div class="form-row">
        <label for="passwd">Password (Leave empty to retain current password)</label>
        <input type="password" name="passwd" id="passwd"  />
    </div>
    <div class="form-row">
    	<? emit_error_msg('email'); ?>
        <label for="email">E-mail address</label>
        <input type="text" name="email" id="email" value="<? print $par['email'] ?>" />
    </div>
    <div class="form-row">
        <input type="submit" id="edit_parent" name="edit" value="Save changes" />
        <input type="submit" name="do_not_edit" value="Discard changes" />
    </div>
</form>

<form id="add_parent_form" method="POST" style="display: <?php print $add_visible ?>" >
    <h3>New Parent</h3>
    <input type="hidden" name="do_add" />
    <div class="form-row">
        <label for="fname">First Name</label>
        <input type="text" name="fname" id="fname"  />
    </div>
    <div class="form-row">
        <label for="lname">Last Name</label>
        <input type="text" name="lname" id="lname"  />
    </div>
    <div class="form-row">
    	<? emit_error_msg('username'); ?>
        <label for="username">User Name</label>
        <input type="text" name="username"  />
    </div>
    <div class="form-row">
        <label for="passwd">Password</label>
        <input type="password" name="passwd" id="passwd"  />
    </div>
    <div class="form-row">
    	<? emit_error_msg('email'); ?>
        <label for="email">E-mail address</label>
        <input type="text" name="email" id="email"  />
    </div>
    <?php emit_error_msg('add_parent_error'); ?>
    <div class="form-row">
        <input type="submit" id="add_parent" name="add" value="Add parent to database" />
        <input type="submit"  name="do_not_add" value="Discard new parent" />
    </div>
</form>

<hr style="display: <?php print $hr_visible ?>" />

<div id="parent_list">
    <form method="POST">
    <div class="form-row">
        <input type="submit" name="add" value="Create new parent" />
    </div>
    <div class="form-row">
        <select name="pid">
            <option value="null">&lt;parents&gt;</option>
<?php
    $parents = get_parents();

    foreach($parents as $p) {
        printf("            <option value=%s>%s, %s</option>\n",
            $p['pid'], $p['lname'], $p['fname']);
    }
    
    print '        </select>
        <input type="submit" name="edit" value="Edit selected parent" />
        <input type="submit" name="delete" value="Delete selected parent" />
    </div>
';

?>
    </form>
</div>
<?php html_footer(); ?>
