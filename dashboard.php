<?php include './includes/header.php'; ?>



<div class="container-fluid">

	<div class="row">
		<nav class="col-md-2 d-none peach-gradient d-md-block bg-light sidebar">
			<div class="sidebar-sticky">
				<ul class="nav flex-column">
					<br>
					<li class="nav-item">
						<a class="nav-link active" href="#">
							<span data-feather="home"></span>
							Dashboard <span class="sr-only">(current)</span>
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="file"></span>
							Personal Details
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="bar-chart-2"></span>
							Summary
						</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#">
							<span data-feather="users"></span>
							Patients Added 
						</a>
					</li>
					<!--
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
					-->
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
					<!--
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
					-->
				</ul>
			</div>
		</nav>

		<main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
			<style type="text/css">
				h1,h2,h3,h4{
					color: #6D6C6B;
					font-family: Montserrat;
				}
			</style>
			<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
				<h1 class="h2">Dashboard</h1>
				<?php include './includes/clock.php' ?>
			</div>

			<?php
				include './includes/connection.php';
				$sql = "SELECT * FROM admin_users WHERE id_admin = " . $_SESSION['adminId'] . ";";
				$result = mysqli_query($conn, $sql);
				$data = mysqli_fetch_assoc($result);
				$dob = $data['dateOfBirth'];
				$deg = $data['degree'];
				$email = $data['email_admin'];
			?>
			<h2>Hello Dr. <?php echo $_SESSION['firstName'] . " " . $_SESSION['lastName']; ?></h2>
			<h4>Personal Details</h4>
			<h4>Date Of Birth : <?php echo $dob; ?></h4>
			<h4>Degree : <?php echo $deg; ?></h4>
			<h4>Email Id : <?php echo $email; ?></h4>

			<hr>
			<!-- SUMMARY  -->
			<h2>Summary</h2>
			<?php 
				include './includes/connection.php';
				include './includes/earning.php';
			?>
			<div class="row">
				&emsp;
				<div class="card text-white bg-primary mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Today's Summary</div>
					<div class="card-body">
						<h5 class="card-title"> New patients : <?php echo $todayNewCount; ?> </h5>
						<h5 class="card-title"> Follow Ups : <?php echo $todayFollowCount; ?></h5>
						<p class="card-text"> Earning : <?php echo $todayEarnCount; ?></p>
					</div>
				</div>
				&emsp;
				<div class="card text-white bg-secondary mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Week's Summary</div>
					<div class="card-body">
						<h5 class="card-title"> New patients : <?php echo $weekNewCount; ?></h5>
						<h5 class="card-title"> Follow Ups : <?php echo $weekFollowCount; ?></h5>
						<p class="card-text"> Earning : <?php echo $weekEarnCount; ?></p>
					</div>
				</div>
				&emsp;
				<div class="card text-white bg-success mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Month's Summary</div>
					<div class="card-body">
						<h5 class="card-title"> New patients : <?php echo $monthNewCount; ?></h5>
						<h5 class="card-title"> Follow Ups : <?php echo $monthFollowCount; ?></h5>
						<p class="card-text"> Earning : <?php echo $monthEarnCount; ?></p>
					</div>
				</div>
				&emsp;
				<div class="card text-white bg-warning mb-3 col-sm" style="max-width: 18rem;">
					<div class="card-header">Year's Summary</div>
					<div class="card-body">
						<h5 class="card-title"> New patients : <?php echo $yearNewCount; ?></h5>
						<h5 class="card-title"> Follow Ups : <?php echo $yearFollowCount; ?></h5>
						<p class="card-text"> Earning : <?php echo $yearEarnCount; ?></p>
					</div>
				</div>
			</div><hr>

			<!--
			<canvas class="my-4 w-100" id="myChart" width="450" height="190"></canvas>
			-->
			
			<h2>Patients Added Last Week</h2>
			
			<table class="table table-striped text-center">
                <thead class="purple-gradient white-text">
                    <tr>
                        <th width="14%" height="36"><div align="center">Patient Name</div></th>
                        <th width="11%"><div align="center">Date Joined</div></th>
                        <th width="20%"><div align="center">Address</div></th>   
                        <th width="10%"><div align="center">Paid</div></th> 
                        <th width="10%"><div align="center">Balance</div></th>
                        <th width="15%"><div align="center">Treatment</div></th>
                        <th width="20%"><div align="center">Action</div></th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $weekNewTab = "select id, firstName, lastName, dateJoined, address, phone, paid,balance, treatment from patient_data where  `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY ) ORDER BY dateJoined DESC;";

                    /*
                    $weekFollow = "select COUNT(id) as count from follow_up_data where  `date` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);";
                    $weekFollowCount = calculate($conn,$weekFollow);
					*/




                    $qsql = mysqli_query($conn,$weekNewTab);

                    while($rs = mysqli_fetch_assoc($qsql))
                    {
                        echo "<tr>
                        
                        <td>$rs[firstName] $rs[lastName] 
                        </td>
                        
                        <td>
                        <strong>Date</strong>: $rs[dateJoined]
                        </td>
                        
                        <td>$rs[address]<br><strong>Phone No :</strong> $rs[phone]
                        </td>
                        <td>Rs $rs[paid]</td>
                        
                        <td>Rs $rs[balance]</td>
                        <td>$rs[treatment]</td>

                        <td align='center'>";
                        
                            echo "<a class=\"btn-sm white-text purple-gradient\" href='patientDetails.php?id=$rs[id]'>View Report</a>";
                        
                        echo "</td></tr>";
                    }
                    ?>
                </tbody>
            </table>
            <hr>



            <h2>Follow Ups Last Week</h2>

			<table class="table table-striped text-center">
                <thead class="purple-gradient white-text">
                    <tr>
                        <th width="14%" height="36"><div align="center">Patient Name</div></th>
                        <th width="11%"><div align="center">Date Joined</div></th>
                        <th width="20%"><div align="center">Address</div></th>    
                        <th width="10%"><div align="center">Paid</div></th>
                        <th width="10%"><div align="center">Balance</div></th>
                        <th width="15%"><div align="center">Treatment</div></th>
                        <th width="20%"><div align="center">Action</div></th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    
                    $weekFollowTab = "select patient_data.id, firstName, lastName, dateJoined, address, phone, follow_up_data.balance,follow_up_data.paid, follow_up_data.treatment from follow_up_data INNER JOIN patient_data ON follow_up_data.id = patient_data.id where  `date` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);";

                    $qsql = mysqli_query($conn,$weekFollowTab);

                    while($rs = mysqli_fetch_assoc($qsql))
                    {
                        echo "<tr>
                        
                        <td>$rs[firstName] $rs[lastName] 
                        </td>
                        
                        <td>
                        <strong>Date</strong>: $rs[dateJoined]
                        </td>
                        
                        <td>$rs[address]<br><strong>Phone No :</strong> $rs[phone]
                        </td>
                        <td>Rs $rs[paid]</td>                        
                        <td>Rs $rs[balance]</td>
                        <td>$rs[treatment]</td>

                        <td align='center'>";
                        
                            echo "<a class=\"btn-sm white-text purple-gradient\" href='patientDetails.php?id=$rs[id]'>View Report</a>";
                        
                        echo "</td></tr>";
                    }
                    ?>
                </tbody>
            </table>




			<hr>
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