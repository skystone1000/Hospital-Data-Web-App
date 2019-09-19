<?php include './includes/header.php'; ?>

<?php

$id = $_GET['id'];
include './includes/connection.php';

$sql = "SELECT * FROM patient_data WHERE id LIKE '%$id%';";
$result = mysqli_query($conn, $sql);
$data = mysqli_fetch_assoc($result);

?>

<br>
<div class="container">

<h2>Case Details : <?php echo $data['firstName'] ?> <?php echo $data['lastName'] ?></h2><br>

<div class="accordion">
	<div class="card">
		<div class="card-header" id="headingTwo">
			<h2 class="mb-0">
				<button class="btn collapsed" type="button" data-toggle="collapse" data-target="#mainRecord" aria-expanded="false" aria-controls="mainRecord">
					Case Details
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


<br>
<h2>Follow Ups : </h2><br>
<div class="accordion" id="accordionExample">

<?php 
	
$sqlF = "SELECT * FROM follow_up_data WHERE id LIKE '%$id%' ORDER BY id;";
$resultF = mysqli_query($conn, $sqlF);
$resultNumF = mysqli_num_rows($resultF);

		# If results are not null
		if ($resultNumF > 0) {

			while($dataRowF = mysqli_fetch_assoc($resultF)){
				echo '
				<div class="card">
					<div class="card-header" id="headingOne">
					<h2 class="mb-0">
					<button class="btn btn-link collapsed" type="button" data-toggle="collapse" data-target="#followUp'.$dataRowF['follow_up_num'].'" aria-expanded="true" aria-controls="followUp'.$dataRowF['follow_up_num'].'">
					Follow Up Number : '.$dataRowF['follow_up_num'].' 
					</button>
					</h2>
					</div>

				<div id="followUp'.$dataRowF['follow_up_num'].'" class="collapse show" aria-labelledby="headingOne" data-parent="#accordionExample">
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
			echo "<h1>No Follow Ups Found !!!</h1>";
		}
?>	
		</div>
	</div>
</div>
</div>

<?php include './includes/footer.php'; ?>