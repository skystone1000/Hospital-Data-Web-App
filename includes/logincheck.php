<?php

if (isset($_POST['login-submit'])){
	require 'connection.php';
	$mailuid = $_POST['mailuid'];
	$password = $_POST['pwd'];

	if (empty($mailuid) || empty($password)){
		header("Location: ../index.php?error=emptyfields");
		exit();
	}
	else {
		/*
		$mailuid = filter_var($mailuid, FILTER_SANITIZE_EMAIL);

		if (!filter_var($mailuid, FILTER_VALIDATE_EMAIL) === true){
			header("Location: ../index.php?error=EmailHasErrors ");
			exit();
		} 

		else {
		*/
			$sql = "SELECT * FROM admin_users WHERE email_admin=? OR uid_admin=? ; ";
			$stmt = mysqli_stmt_init($conn);
			if (!mysqli_stmt_prepare($stmt, $sql)) {
				header("Location: ../index.php?error=sqlerror");
				exit();
			}
			else {
				mysqli_stmt_bind_param($stmt, "ss", $mailuid, $mailuid);
				mysqli_stmt_execute($stmt);
				$result = mysqli_stmt_get_result($stmt);
				if ($row = mysqli_fetch_assoc($result)) {
					//$pwdCheck = password_verify($password, $row['password_admin']);
					//if ($pwdCheck == false){
					if ($password != $row['password_admin'] ){
						header("Location: ../index.php?error=wrongPassword1");
						exit();
					}
					else if ($password == $row['password_admin']) {
						session_start();
						$_SESSION['adminId'] = $row['id_admin'];
						$_SESSION['adminUid'] = $row['uid_admin'];
						$_SESSION['firstName'] = $row['firstName'];
						$_SESSION['lastName'] = $row['lastName'];

						header("Location: ../home.php?login==success");
						exit();
					}
					else {
						header("Location: ../index.php?error=wrongPassword2");
						exit();
					}
				}
				else {
					header("Location: ../index.php?error=noUser");
					exit();
				}
			}
		//}
	}
}
else {
	header("Location: ../index.php");
	exit();
}

?>