<?php
session_start();
include("functions.php");
divert_from_login();
html_header("Administrator Login");
html_banner("Administrator Login");
emit_error_msg('login'); ?>

    <form action="dologin.php" class="login-form"  method="post" name="loginForm">

        <div class="form-row">
            <label for="user_name">User Name</label>
            <input type="text" name="user_name"  />
        </div>
        
        <div class="form-row">
            <label for="user_pass">Password</label>
            <input name="user_pass" type="password"  />
        </div>

    
        <div class="form-row">
            <input name="login" type="submit" value="Login"  />
        </div>

        <input type="hidden" name="user_type" value="admin" />

    </form>

<?php html_footer(); ?>
