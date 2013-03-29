<?php
include("functions.php");
session_start();
divert_from_login();
html_header("Parent Login");
html_banner("Welcome"); ?>

<!--login-->
    <form action="dologin.php" class="login-form"  method="post" name="loginForm">

        <div class="form-row">
            <label for="user_name">User Name</label>
            <input type="text" name="user_name"  />
        </div>
        
        <div class="form-row">
            <label for="user_pass">Password</label>
            <input name="user_pass" type="password"  />
        </div>

        <?php emit_error_msg('login'); ?>
    
        <div class="form-row">
            <input name="login" type="submit" value="Login"  />
        </div>

        <input type="hidden" name="user_type" value="parent" />

    </form>
    <div class="loginMessageClass" align="center">
        <font color="#FF0000">*</font> There is no registration. Please use your SCS email and the randomly generated password that was sent to you.<br />
        Please Call 416-483-4835 for more information.
        <p class="admin-link"><a href="admin-login.php">Admin Login</a></p>
    </div>

<?php html_footer(); ?>
