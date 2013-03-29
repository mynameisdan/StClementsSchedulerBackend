<?php

    session_start();
    include_once("functions.php"); 
    include_once("db_conn.php"); 

    require_login('admin');
    $adm = get_admin_user($_SESSION['admin']['username'], null);

    if (isset($_POST['change_pw'])) {
        if ($par['passwd'] != md5($_POST['old_pass'])) {
            set_error_msg('settings', "You entered your current password incorrectly.");
        } else if ($_POST['new_pass1'] != $_POST['new_pass2']) {
            set_error_msg('settings', "Your new passwords do not match.");
        } else if (strlen($_POST['new_pass1']) < 8) {
            set_error_msg('settings', "Your new password must be at least 8 characters.");
        } else {
            $ret = update_admin_user($adm['username'], $_POST['new_pass1']);
            if ($ret != 1) {
                set_error_msg('settings', "An error occurred.");
            } else {
                set_info_msg('settings', "Password successfully changed.");
            }
        }
    }
    
    html_header("Change Password");
    html_banner("Change Password");

    emit_info_msg('settings');
    emit_error_msg('settings');
?>

<form id="change_password" method="post">
    <h3>Change password</h3><p>Must be at least 8 characters.</p>
    <input type="hidden" name="username" id="username" value="<? print $_SESSION['admin']['username'] ?>" />
    <div class="form-row">
        <label for="old_pass">Current password</label>
        <input type="password" id="old_pass" name="old_pass" />
    </div>
    <div class="form-row">
        <label for="new_pass1">New password</label>
        <input type="password" id="new_pass1" name="new_pass1" />
        <label for="new_pass2">Confirm new password</label>
        <input type="password" id="new_pass2" name="new_pass2" />
    </div>
    <div class="form-row">
        <input type="submit" id="change_password_button" name="change_pw" value="Change password" />
    </div>
</form>

<?php html_footer(); ?>
