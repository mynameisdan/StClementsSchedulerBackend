<?php

    session_start();
    include_once("functions.php"); 
    include_once("db_conn.php"); 

    require_login('parent');
    $par = get_parent($_POST['pid']);

    if (isset($_POST['change_pw'])) {
        if ($par['passwd'] != md5($_POST['old_pass'])) {
            set_error_msg('settings', "You entered your current password incorrectly.");
        } else if ($_POST['new_pass1'] != $_POST['new_pass2']) {
            set_error_msg('settings', "Your new passwords do not match.");
        } else if (strlen($_POST['new_pass1']) < 8) {
            set_error_msg('settings', "Your new password must be at least 8 characters.");
        } else {
            $ret = update_parent($par['pid'], $par['fname'], $par['lname'],
                $par['username'], $_POST['new_pass1'], $par['email']);
            if ($ret != 1) {
                set_error_msg('settings', "An error occurred.");
            } else {
                set_info_msg('settings', "Password successfully changed.");
            }
        }
    }

	if (isset($_POST['change_email'])) {
		$new_email = trim($_POST['email']);
		if (filter_var($new_email, FILTER_VALIDATE_EMAIL)) {
			$ret = update_parent($par['pid'], $par['fname'], $par['lname'],
                $par['username'], $par['passwd'], $new_email);
            if ($ret != 1) {
                set_error_msg('settings', "An error occurred.");
            } else {
                set_info_msg('settings', "E-mail successfully changed.");
            }
		} else {
            set_error_msg('settings', "Please enter a valid e-mail address.");
		}
    }
   

    html_header("Account Settings");
    html_banner("Account Settings");

    emit_info_msg('settings');
    emit_error_msg('settings');
?>

<form id="change_password" method="post">
    <h3>Change password</h3>
    <input type="hidden" name="pid" id="pid" value="<? print $_SESSION['parent']['pid'] ?>" />
    <div class="form-row">
        <label for="old_pass">Current password</label>
        <input type="password" id="old_pass" name="old_pass" />
    </div>
    <div class="form-row">
        <label for="new_pass1">New password (must be at least 8 characters)</label>
        <input type="password" id="new_pass1" name="new_pass1" />
        <label for="new_pass2">Confirm new password (must be at least 8 characters)</label>
        <input type="password" id="new_pass2" name="new_pass2" />
    </div>
    <div class="form-row">
        <input type="submit" id="change_password_button" name="change_pw" value="Change password" />
    </div>
</form>

<form id="change_email" method="post">
    <h3>Change email address</h3>
    <input type="hidden" name="pid" id="pid" value="<? print $_SESSION['parent']['pid'] ?>" />
    <div class="form-row">
        <label for="email">Email address (If you wouldn't like to receive any email, just enter a blank address.)</label>
        <input type="text" id="email" name="email" />
    </div>
    <div class="form-row">
        <input type="submit" id="change_email" name="change_email" value="Change email" />
    </div>
</form>


<?php html_footer(); ?>
