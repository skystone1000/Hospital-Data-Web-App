<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Patient Records</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="./css/style.css">

</head>

<style type="text/css">

form{
	line-height: 30px;
}

img{
	max-height: 350px;
	max-width: 200px;
}
h3{
	color: green;
}

.docLogo{
	background-image: url(./img/docLogo.png);
	background-size: 25vw;
	background-repeat: no-repeat;
	background-position: center;
	display: none;
}
@media (max-width: 1000px) { 
	.fillForm { min-width: 100%; }
}

@media(min-width: 1000px) {
	.docLogo { display: block; }
}

.heading{
	background-color: #F3FFF1;
	border-radius: 10px;
	box-shadow: 0 4px 10px 4px rgba(19, 35, 47, 0.3);
	transition: .5s ease;
	padding:30px;
}

.form{
	background-color: #F3FFF1;
	padding:40px;
	max-width: 500px;
	margin: 40px auto;
	border-radius: 10px;
	box-shadow: 0 4px 10px 4px rgba(19, 35, 47, 0.3);
	transition: .5s ease;
	text-align: center;	
	line-height: 2;
}

.form:hover {
	box-shadow: 0px 0px 40px 16px rgba(18,18,18,1.00);
}

</style>
<body>
	<br>
	<div class="container text-center border heading">
		<h1>Mahajan Homeo Clinic & Research Center</h1>
		<h3>.....  Hope for HopeLess  .....</h3>
	</div>
	<br>
	<div class="container">
		<div class="row">
			<div class="col-sm-4 docLogo"></div>

			<div class="col-sm-4 fillForm">
				<form class="site-form form" action="./includes/logincheck.php" method="post">
					<h3>Log In</h3>

					<div class="form-row">
						<div class="form-group col-md-12">
							<label for="mailuid">Email</label>
							<input type="email" name="mailuid" class="form-control" id="mailuid" placeholder="Email">
						</div>
					</div>
					<div class="form-row">
						<div class="form-group col-md-12">
							<label for="pwd">Password</label>
							<input type="password" name="pwd" class="form-control" id="pwd" placeholder="Password">
						</div>
					</div>
					<br>
					<button type="submit" name="login-submit" class="btn btn-success">Sign in</button>
				</form>
			</div>

			<div class="col-sm-4 docLogo"></div>
		</div>
	</div>

<?php
	include './includes/footer.php';
?>