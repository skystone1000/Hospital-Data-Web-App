<?php
	include './headFoot/header.php';
?>

<div class="container">
	<br>
	<h1>List Of Patients </h1>
	<div class="list-group">
	
	<?php
		include './headFoot/filter.php';
		include './headFoot/connection.php';
 		
		$result = mysqli_query($conn, $sql);
		$resultNum = mysqli_num_rows($result);

		# If results are not null
		if ($resultNum > 0) {

			# Output total number of results
			echo "<h2>" . $resultNum . " Results ... </h2>";

			# Information Card
			echo '
			<div class="list-group-item list-group-item-action flex-column align-items-start">
				<div class="d-flex w-100 justify-content-between">
					<h5 class="mb-1">Registration Number : Name</h5>
				</div>
			</div>
			';

			# Each Patient record card
			while($row = mysqli_fetch_assoc($result)){
				echo '
				<div class="list-group-item list-group-item-action flex-column align-items-start active">
					<div class="d-flex justify-content-between row">
						<h5 class="mb-1 col-8">' . $row['regno'] . " : " . $row['firstName'] . " " . $row['middleName'] . " " . $row['lastName']  . '</h5>
						<div class="col"><a href="patientDetails.php?id=' . $row['id'] . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Details</button></a></div>
						<div class="col"><a href="deleteRecord.php?id=' . $row['id'] . '" onclick="return confirm(\'Are you sure you want to delete this item?\');"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Delete</button></a></div>

					</div>
				</div>
				';

				/*
				<a href="deleteRecord.php?id=' . $row['id'] . '" onclick="return confirm(\'Are you sure you want to delete this item?\');">
				onclick is used for confirmation popup
				*/
			}
		} 
		# If no results found
		else {
			echo "<h1>No Results Found !!!</h1>";
		}
	?>	
		
	</div>


</div>

<?php
	include './headFoot/footer.php';
?>