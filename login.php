<?php
	include './headFoot/header.php'
?>

<style type="text/css">

form{
	line-height: 30px;
}

img{
	max-height: 350px;
	max-width: 200px;
}

.docLogo{
	background-image: url(./img/docLogo.png);
	background-size: 300px;
	background-repeat: no-repeat;
	background-position: center;
}
@media (max-width: 576px) { 
	.docLogo { display: none; }
}

@media(min-width: 576px) {
	.fillForm { min-width: 45%; }
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
	<br><br>
	<div class="container">
		<div class="row">
			<div class="col docLogo"></div>

			<div class="col fillForm">
				<form class="site-form form" action="" method="post">
					<h1>Log In</h1>

					<div class="form-row">
						<div class="form-group col-md-12">
							<label for="inputEmail4">Email</label>
							<input type="email" class="form-control" id="inputEmail4" placeholder="Email">
						</div>
					</div>
					<div class="form-row">
						<div class="form-group col-md-12">
							<label for="inputPassword4">Password</label>
							<input type="password" class="form-control" id="inputPassword4" placeholder="Password">
						</div>
					</div>
					<br>
					<button type="submit" class="btn btn-success">Sign in</button>
				</form>
			</div>

			<div class="col docLogo"></div>
		</div>
	</div>

<?php
	include './headFoot/footer.php';
?>