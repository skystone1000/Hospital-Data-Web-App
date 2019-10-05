<?php
	session_start();

	if (isset($_SESSION['adminId'])) {
		//echo '<p class="login-status">You are Logged in</p>';
	}
	else{
		header("Location: ./index.php?error=PleaseLogin");
	}
?>

<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Patient Records</title>

		<!-- Font Awesome -->
	<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">
	<!-- Bootstrap core CSS -->
	<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">
	<!-- Material Design Bootstrap -->
	<link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.8.10/css/mdb.min.css" rel="stylesheet">
	<link rel="stylesheet" type="text/css" href="./css/style.css">
	<link href="https://fonts.googleapis.com/css?family=Montserrat&display=swap" rel="stylesheet">
	<style type="text/css"> 
        body{
            background: linear-gradient(to right, #FFFFFF, #DCD6D1); /* W3C, IE 10+/ Edge, Firefox 16+, Chrome 26+, Opera 12+, Safari 7+ */
        }
    </style>

</head>
<body>
	<nav class="navbar aqua-gradient navbar-expand-lg navbar-light bg-light">
		<a class="navbar-brand" href="#">Mahajan Homeo Clinic</a>
		<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>

		<div class="collapse navbar-collapse" id="navbarSupportedContent">
			<ul class="navbar-nav mr-auto">
				<li class="nav-item active">
					<a class="nav-link" href="dashboard.php">
						Dashboard 
						<span class="sr-only">(current)</span>
						<img src="./img/dashboard.svg" style="width: 20px ; height: 20px;">
					</a>
				</li>
				<li class="nav-item active">
					<a class="nav-link" href="home.php">
						Home 
						<span class="sr-only">(current)</span>
						<img src="./img/house.svg" style="width: 20px ; height: 20px;">
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="fillForm.php">
						Add Patient
						<img src="./img/add-patient.svg" style="width: 20px ; height: 20px;">
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="records.php">
					Patient Records
					<img src="./img/report.svg" style="width: 20px ; height: 20px;">
				</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="detailedRecords.php">
					Detailed Patient Records
					<img src="./img/detailedreport.svg" style="width: 20px ; height: 20px;">
				</a>
				</li>
				<!--
				<li class="nav-item dropdown">
					<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
						Dropdown
					</a>
					<div class="dropdown-menu" aria-labelledby="navbarDropdown">
						<a class="dropdown-item" href="#">Action</a>
						<a class="dropdown-item" href="#">Another action</a>
						<div class="dropdown-divider"></div>
						<a class="dropdown-item" href="#">Something else here</a>
					</div>
				</li>
				-->
			</ul>
			<form action="search.php" method="get" class="form-inline my-2 my-lg-0" >
				<input class="form-control mr-sm-2" type="search" placeholder="Search" aria-label="Search" name="dataSearch">
				<button class="heavy-rain-gradient btn btn-outline-success my-2 my-sm-0" type="submit" value="submit">Search</button>
			</form>
		</div>
	</nav>

