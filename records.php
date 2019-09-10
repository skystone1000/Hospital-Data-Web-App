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
				include './includes/recordCard.php';
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
	include './includes/footer.php';
?>