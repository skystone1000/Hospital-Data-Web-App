<?php include './includes/header.php'; ?>

<?php

$id = filter_var($_GET['id'] ?? 0, FILTER_VALIDATE_INT);
include './includes/connection.php';

$stmt = $conn->prepare("SELECT * FROM patient_data WHERE id = ?");
$stmt->bind_param("i", $id);
$stmt->execute();
$data = $stmt->get_result()->fetch_assoc();

?>

<br>
<div class="container-fluid">

	<div class="row">
		<div class="col">
			<h3>Case Details : <?php echo h($data['firstName']) ?> <?php echo h($data['lastName']) ?></h3>
		</div>
		<div class="col">
			<h4>Registration No : <?php echo h($data['regno']) ?></h4><br>
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

		$stmtF = $conn->prepare("SELECT * FROM follow_up_data WHERE id = ? ORDER BY CAST(follow_up_num AS UNSIGNED) DESC");
		$stmtF->bind_param("i", $id);
		$stmtF->execute();
		$resultF = $stmtF->get_result();
		$resultNumF = $resultF->num_rows;

		# If results are not null
		if ($resultNumF > 0) {

			while($dataRowF = $resultF->fetch_assoc()){
				echo '
				<div class="card">
				<div class="card-header" id="headingOne">
				<h2 class="mb-0">
				<button class="btn btn-info show " type="button" data-toggle="collapse" data-target="#followUp'.h($dataRowF['follow_up_num']).'" aria-expanded="true" aria-controls="followUp'.h($dataRowF['follow_up_num']).'">
				Follow Up Number : '.h($dataRowF['follow_up_num']).'
				---- Patient ID : '.h($dataRowF['id']).'
				---- Registration No : '.h($dataRowF['regno']).'
				---- Date : '.h($dataRowF['date']).'
				</button>
				</h2>
				</div>

				<div id="followUp'.h($dataRowF['follow_up_num']).'" class="collapse" aria-labelledby="headingOne" data-parent="#accordionExample">
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
						Case Details ---- Date Joined : <?php echo h($data['dateJoined']) ?>
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