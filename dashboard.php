<?php include './includes/header.php'; ?>



<div class="container-fluid">

	<div class="row">
		<nav class="col-md-2 d-none d-md-block bg-light sidebar">
			<div class="sidebar-sticky">
				<ul class="nav flex-column">
					<li class="nav-item">
						<a class="nav-link active" href="#">
							<span data-feather="home"></span>
							Dashboard <span class="sr-only">(current)</span>
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="file"></span>
							Orders
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="shopping-cart"></span>
							Products
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="users"></span>
							Customers
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="bar-chart-2"></span>
							Reports
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="layers"></span>
							Integrations
						</a>
					</li>
				</ul>

				<h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">
					<span>Saved reports</span>
					<a class="d-flex align-items-center text-muted" href="#">
						<span data-feather="plus-circle"></span>
					</a>
				</h6>
				<ul class="nav flex-column mb-2">
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="file-text"></span>
							Current month
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="file-text"></span>
							Last quarter
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="file-text"></span>
							Social engagement
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="file-text"></span>
							Year-end sale
						</a>
					</li>
				</ul>
			</div>
		</nav>

		<main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
			
			<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
				<h1 class="h2">Dashboard</h1>
				<?php include './includes/clock.php' ?>
			</div>

			<div class="row">
				&emsp;
				<div class="card text-white bg-primary mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Today's Summary</div>
					<div class="card-body">
						<h5 class="card-title">Total new patients : </h5>
						<h5 class="card-title">Total Follow Ups : </h5>						
						<p class="card-text">Total Earning : </p>
					</div>
				</div>
				&emsp;&emsp;&emsp;
				<div class="card text-white bg-secondary mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Week's Summary</div>
					<div class="card-body">
						<h5 class="card-title">Total new patients : </h5>
						<h5 class="card-title">Total Follow Ups : </h5>						
						<p class="card-text">Total Earning : </p>
					</div>
				</div>
				&emsp;&emsp;&emsp;
				<div class="card text-white bg-success mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Month's Summary</div>
					<div class="card-body">
						<h5 class="card-title">Total new patients : </h5>
						<h5 class="card-title">Total Follow Ups : </h5>						
						<p class="card-text">Total Earning : </p>
					</div>
				</div>

			</div>

			<!--
			<canvas class="my-4 w-100" id="myChart" width="450" height="190"></canvas>
			-->
			
			<h2>Patients Added Last Week</h2>
			
		</main>
	</div>
</div>

<!-- Bootstrap core JavaScript
	================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>

	<!-- Icons -->
	<script src="https://unpkg.com/feather-icons/dist/feather.min.js"></script>
	<script>
		feather.replace()
	</script>

	<?php include './includes/footer.php'; ?>