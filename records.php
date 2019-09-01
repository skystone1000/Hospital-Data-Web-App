<?php
	include './headFoot/header.php';
?>

<div class="container">
	<br>
	<h1>List Of Patients </h1>
	<div class="list-group">
	
	<div>
		<button type="button" class="btn btn-success dropdown-toggle" data-toggle="dropdown">
			Filter
			<img src="./img/filter.svg" style="width: 30px ; height: 30px;">
		    <ul class="dropdown-menu">
				<!--<input class="form-control" id="myInput" type="text" placeholder="Search..">-->
				<li><a href="<?php $sort = fname ?>">First Name</a></li>
				<li><a href="<?php $sort = lname ?>">Last Name</a></li>
				<li><a href="<?php $sort = date ?>">Date</a></li>
		    </ul>

		</button>
	</div>	

	<?php
		include './headFoot/connection.php';
 		
 		echo $sort;
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




<?php
	include './headFoot/footer.php';
?>