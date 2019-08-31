<?php
	include './headFoot/header.php';
?>

<div class="container">
	<br>
	<h1>List Of Patients </h1>
	<div class="list-group">
		

	<?php
		include './headFoot/connection.php';
 
		$sql = "SELECT * FROM patient_data;";
		$result = mysqli_query($conn, $sql);
		$resultNum = mysqli_num_rows($result);

		if ($resultNum > 0) {
			echo "<h2>" . $resultNum . " Results ... </h2>";
			//include './headFoot/detailsCard.php';
			echo '
			<div class="list-group-item list-group-item-action flex-column align-items-start">
				<div class="d-flex w-100 justify-content-between">
					<h5 class="mb-1">Registration Number : Name</h5>
				</div>
			</div>
			';

			while($row = mysqli_fetch_assoc($result)){
				echo '
				<div class="list-group-item list-group-item-action flex-column align-items-start active">
					<div class="d-flex justify-content-between row">
						<h5 class="mb-1 col-8">' . $row['regno'] . " : " . $row['firstName'] . " " . $row['middleName'] . " " . $row['lastName']  . '</h5>
						<div class="col"><a href="patientDetails.php?id=' . $row['id'] . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Details</button></a></div>
						<div class="col"><a href="deleteRecord.php?id=' . $row['id'] . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Delete</button></a></div>

					</div>
				</div>
				';
			}
		} else {
			echo "<h1>No Results Found !!!</h1>";
		}
	?>	
		
	</div>


</div>




</body>
	<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
</html>