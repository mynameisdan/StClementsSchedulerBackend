<?php

    include_once("functions.php");

	session_start();
	
	if(isset($_SESSION)) {
		unset($_SESSION['admin']);
		unset($_SESSION['parent']);
		
		session_destroy();
	}

	header('Location: ' . $urls['index']);

?>
