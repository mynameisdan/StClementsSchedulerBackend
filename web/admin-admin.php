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
            if ( add_admin_user($_POST['username'], $_POST['passwd']) ) {
                set_info_msg('admin-admin', 'Successfully added admin');
            } else {
                set_info_msg('admin-admin', 'Failed to add admin');
            }
        } else {
            // We're just loading the form.
            $add_visible = "inline";
            $hr_visible = "block";
        }

    } else if ( isset($_POST['username'])
        && ($_POST['username'] == $_SESSION['admin']['username'])) {

        set_info_msg('admin-admin',
            'You cannot operate on your account while you are logged in.');

    } else if (isset($_POST['username']) && $_POST['username'] != "null") {

        if (isset($_POST['delete'])) {
            
            if (isset($_POST['do_delete'])) {
                // This was a form submission, change the DB
                $ret = delete_admin_user($_POST['username']);
                if ( $ret ) {
                    set_info_msg('admin-admin', 'Successfully deleted admin ' . $_POST['username']);
                } else {
                    set_info_msg('admin-admin', 'Failed to delete admin ' . $_POST['username']);
                }
            } else {
                // We're just loading the form
                $adm = get_admin_user($_POST['username'], NULL);
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

                if ( update_admin_user($_POST['username'], $_POST['passwd']) ) {
                    set_info_msg('admin-admin', 'Saved changes to admin ' . $_POST['username']);
                } else {
                    set_info_msg('admin-admin', 'Failed to update admin ' . $_POST['username']);
                }
            } else {
                // We're just loading the form
                $adm = get_admin_user($_POST['username'], NULL);
                $edit_visible = "inline";
                $hr_visible = "block";
            }
        }
    }

    // Messages for cancelled actions
    if (isset($_POST['do_not_add'])) {
        set_info_msg('admin-admin', 'Admin was not added.');
    } else if (isset($_POST['do_not_delete'])) {
        set_info_msg('admin-admin', 'Admin was not deleted.');
    } else if (isset($_POST['do_not_edit'])) {
        set_info_msg('admin-admin', 'Admin was not modified.');
    }

// Page rendering starts...

	html_header("Administration: Admin Users");
	html_banner("Manage Admin Users"); 

    emit_info_msg('admin-admin');
    emit_error_msg('db_exception');
?>


<form id="delete_admin_form" method="POST" style="display: <?php print $delete_visible ?>" >
    <h3>Confirm deletion</h3>
    <input type="hidden" name="do_delete" />
    <input type="hidden" name="username" id="username" value="<? print $adm['username'] ?>" />
    <p class="form-row">Are you sure that you want to delete <em><? print $adm['username'] ?></em>?</p>
    <div class="form-row">
        <input type="submit" name="delete" value="Yes, delete this admin" />
        <input type="submit" name="do_not_delete" value="No, keep this admin" />
    </div>
</form>

<form id="edit_admin_form" method="POST" style="display: <?php print $edit_visible ?>" >
    <h3>Editing admin</h3>
    <input type="hidden" name="do_edit" />
    <input type="hidden" name="username"  value="<? print $adm['username'] ?>"  />
    <div class="form-row">
        <label for="username">User Name</label>
        <input type="text" id="username" disabled="disabled" value="<? print $adm['username'] ?>" />
    </div>
    <div class="form-row">
        <label for="passwd">Password (Leave empty to retain current password)</label>
        <input type="password" name="passwd" id="passwd"  />
    </div>
    <div class="form-row">
        <input type="submit" id="edit_admin" name="edit" value="Save changes to admin" />
        <input type="submit"  name="do_not_edit" value="Discard changes" />
    </div>
</form>

<form id="add_admin_form" method="POST" style="display: <?php print $add_visible ?>" >
    <h3>New administrator</h3>
    <input type="hidden" name="do_add" />
    <div class="form-row">
        <label for="username">User Name</label>
        <input type="text" name="username" id="username" value="<? print $adm['username'] ?>" />
    </div>
    <div class="form-row">
        <label for="passwd">Password</label>
        <input type="password" name="passwd" id="passwd"  />
    </div>
    <?php emit_error_msg('add_admin_error'); ?>
    <div class="form-row">
        <input type="submit" id="add_admin" name="add" value="Add admin to database" />
        <input type="submit"  name="do_not_add" value="Discard new admin" />
    </div>
</form>

<hr style="display: <?php print $hr_visible ?>" />

<div id="admin_list">
    <form method="POST">
    <div class="form-row">
    <input type="submit" name="add" value="Create new admin" />
    </div>
    <div class="form-row">
    <select name="username">
        <option value="null">&lt;admins&gt;</option>
<?php
    $admins = get_admins();

    foreach($admins as $a) {
        printf("    <option value=%s>%s</option>\n",
            $a['username'], $a['username']);
    }
    
    print '    </select>
        <input type="submit" name="edit" value="Edit selected admin" />
        <input type="submit" name="delete" value="Delete selected admin" />
</div>';

?>
    </form>
</div>
<?php html_footer(); ?>
