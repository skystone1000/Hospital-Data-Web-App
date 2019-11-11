<?php include './includes/header.php'; ?>

<?php

$id = $_GET['id'];
include './includes/connection.php';

$sql = "SELECT * FROM patient_data WHERE id LIKE '$id';";
$result = mysqli_query($conn, $sql);
$data = mysqli_fetch_assoc($result);

?>

<br>
<div class="container-fluid">

	<div class="row">
		<div class="col">
			<h3>Case Details : <?php echo $data['firstName'] ?> <?php echo $data['lastName'] ?></h3>
		</div>
		<div class="col">
			<h4>Registration No : <?php echo $data['regno'] ?></h4><br>
		</div>
	</div>

	<div class="row">
		<div class="col-md-2">
			<h3>Follow Ups : </h3>
		</div>
		<div class="col-md-10">
			<?php
			echo '
			<div class="col"><a href="followUp.php?id=' . $id . '" target="_blank"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Add Follow Up</button></a></div>
			'
			?>
			<br>
		</div>
	</div>

	<div class="accordion" id="accordionExample">

		<?php 

		$sqlF = "SELECT * FROM follow_up_data WHERE id LIKE '$id' ORDER BY date DESC;";
		$resultF = mysqli_query($conn, $sqlF);
		$resultNumF = mysqli_num_rows($resultF);

		# If results are not null
		if ($resultNumF > 0) {

			while($dataRowF = mysqli_fetch_assoc($resultF)){
				echo '
				<div class="card">
				<div class="card-header" id="headingOne">
				<h2 class="mb-0">
				<button class="btn btn-info show " type="button" data-toggle="collapse" data-target="#followUp'.$dataRowF['follow_up_num'].'" aria-expanded="true" aria-controls="followUp'.$dataRowF['follow_up_num'].'">
				Follow Up Number : '.$dataRowF['follow_up_num'].'
				---- Patient ID : '.$dataRowF['id'].' 
				---- Registration No : '.$dataRowF['regno'].'
				---- Date : '.$dataRowF['date'].'
				</button>
				</h2>
				</div>

				<div id="followUp'.$dataRowF['follow_up_num'].'" class="collapse" aria-labelledby="headingOne" data-parent="#accordionExample">
				<div class="card-body">
				';
				include './includes/followUpForm.php';

				echo '
				</div></div><br>
				
				';
			}

		}
		# If no results found
		else {
			echo "<h4>No Follow Ups Found !!!</h4>";
		}
		?>	
	</div>
	<br>

	<h2>Initial Details :</h2>

	<div class="accordion">
		<div class="card">
			<div class="card-header" id="headingTwo">
				<h2 class="mb-0">
					<button class="btn btn-info collapsed" type="button" data-toggle="collapse" data-target="#mainRecord" aria-expanded="false" aria-controls="mainRecord">
						Case Details ---- Date Joined : <?php echo $data['dateJoined'] ?>
					</button>
				</h2>
			</div>
			<div id="mainRecord" class="collapse" aria-labelledby="headingTwo" data-parent="#accordionExample">
				<div class="card-body">
					<?php include './includes/patientDetailsForm.php'; ?>
				</div>
			</div>
		</div>
	</div>



</div>


<?php include './includes/footer.php'; ?>