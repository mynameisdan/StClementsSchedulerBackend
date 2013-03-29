<?php

session_start();

include_once("db_conn.php");
include_once("functions.php");

global $urls;

$bad_user_pass="Incorrect username or password";
$bad_login="Login error";

if ($_POST['user_type'] == 'admin') {
    $user = get_admin_user($_POST['user_name'], md5($_POST['user_pass']));
    if ($user) {
        $_SESSION['admin']['logged_in'] = true;
        $_SESSION['admin']['username'] = $_POST['user_name'];
        header('Location: ' . $urls['admin']['home']);
    } else {
        set_error_msg('login', $bad_user_pass);
        header('Location: ' . $urls['admin']['login']);
    }
} else if ( $_POST['user_type'] == 'parent') {
	$open_session = get_open_session();
	$closed_session = get_closed_session();
	
	if(!$open_session && !$closed_session) {
		set_error_msg('login', 'There are no open registration sessions.');
		header('Location: ' . $urls['parent']['login']);
		return;
	}

	if($open_session) {
		$session = $open_session[0];
		$start = strtotime($session['startts']);
		$end = strtotime($session['endts']);
		$now = time();
		
		if($now >= $end || $now <= $start) {
			set_error_msg('login', 'There are no open registration sessions.');
			header('Location: ' . $urls['parent']['login']);
			return;
		}
	}

    $user = get_parent_user($_POST['user_name'], md5($_POST['user_pass']));
    if ($user) {
        $_SESSION['parent']['logged_in'] = true;
        $_SESSION['parent']['username'] = $user['user'];
        $_SESSION['parent']['fname'] = $user['fname'];
        $_SESSION['parent']['lname'] = $user['lname'];
        $_SESSION['parent']['pid'] = $user['pid'];
        $_SESSION['parent']['tb'] = $user['tb'];
        header('Location: ' . $urls['parent']['home']);
    } else {
        set_error_msg('login', $bad_user_pass);
        header('Location: ' . $urls['parent']['login']);
    }
} else {
    set_error_msg('login', $bad_login);
    header('Location: ' . $urls['parent']['login']);
}

?>
