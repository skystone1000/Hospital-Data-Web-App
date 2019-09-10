<?php
	echo '
	<div class="list-group-item list-group-item-action flex-column align-items-start active">
		<div class="d-flex justify-content-between row">
			<h5 class="mb-1 col-8">' . $row['regno'] . " : " . $row['firstName'] . " " . $row['middleName'] . " " . $row['lastName']  . '</h5>
			<div class="col"><a href="followUp.php?id=' . $row['id'] . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">FollowUp</button></a></div>
			<div class="col"><a href="patientDetails.php?id=' . $row['id'] . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Details</button></a></div>
			<div class="col"><a href="./php/deleteRecord.php?id=' . $row['id'] . '" onclick="return confirm(\'Are you sure you want to delete this item?\');"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Delete</button></a></div>
		</div>
	</div>
	';
?>