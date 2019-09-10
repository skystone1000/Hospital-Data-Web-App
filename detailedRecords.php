<?php
	include './includes/header.php';
?>

<div class="container">
	<br>
	<h1>List Of Patients </h1>
	<div class="list-group">
		

	<?php
		include './includes/filter.php';
		include './includes/connection.php';
 
		$sql = "SELECT * FROM patient_data;";
		$result = mysqli_query($conn, $sql);
		$resultNum = mysqli_num_rows($result);

		if ($resultNum > 0) {
			echo "<h2>" . $resultNum . " Results ... </h2>";
			include './includes/detailsCard.php';
			while($row = mysqli_fetch_assoc($result)){
				echo '
				<div class="list-group-item list-group-item-action flex-column align-items-start active">
					<div class="d-flex justify-content-between row">
						<h5 class="col-8">' . $row['regno'] . " : " . $row['firstName'] . " " . $row['middleName'] . " " . $row['lastName']  . '</h5>
						<div class="col"><a href="patientDetails.php?id=' . $row['id'] . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Details</button></a></div>
						<div class="col"><a href="./php/deleteRecord.php?id=' . $row['id'] . '" onclick="return confirm(\'Are you sure you want to delete this item?\');"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Delete</button></a></div>
					</div>
					<p class="mb-1">Age: ' . $row['age'] . ', Occupation: ' . $row['occupation'] . ', Address: ' . $row['address'] . '</p>
					<small>Date : ' . $row['dateJoined'] . ', Phone No: ' . $row['phone'] . '</small>
					
				</div>
				';
				echo "<br>";
			}
		} else {
			echo "<h1>No Results Found !!!</h1>";
		}
	?>	
		
	</div>


</div>




<?php
	include './includes/footer.php';
?>