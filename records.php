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

	<br>
	<div class="container">
		<nav aria-label="Page navigation example">
			<ul class="pagination pg-blue justify-content-center">

				<?php if($page != 0){  ?>
					
					<li class="page-item ">
						<a class="page-link btn-primary text-white" tabindex="-1" href="records.php?page=<?php echo $page; ?>">Previous</a>
					</li>

					<li class="page-item"><a class="page-link"> . . . </a></li>

					<li class="page-item">
						<a class="page-link" href="records.php?page=<?php echo $page; ?>"><?php echo $page; ?></a>
					</li>

				<?php } else { ?>

					<li class="page-item ">
						<a class="page-link btn-light text-white disabled" tabindex="-1" href="records.php?page=<?php echo $page; ?>">Previous</a>
					</li>

					<li class="page-item"><a class="page-link"> &nbsp; </a></li>

				<?php } ?>

				<li class="page-item active">
					<a class="page-link"><?php echo $page+1; ?><span class="sr-only">(current)</span></a>
				</li>
				


				<li class="page-item">
					<a class="page-link" href="records.php?page=<?php echo $page+2; ?>"><?php echo $page+2; ?></a>
				</li>
				


				<li class="page-item"><a class="page-link"> . . . </a></li>

				<li class="page-item ">
					<a class="page-link btn-primary text-white" href="records.php?page=<?php echo $page+2; ?>">Next</a>
				</li>
			</ul>
		</nav>
	</div>

</div>

<?php
	include './includes/footer.php';
?>