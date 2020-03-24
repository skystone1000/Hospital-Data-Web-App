<!-- SORT FILTER DIV -->
<div>		

	<button type="button" id="showBtn" class="btn btn-success dropdown-toggle">
		Show
		<img src="./img/filter.svg" style="width: 30px ; height: 30px;">
	</button>

	<button type="button" id="hideBtn" class="btn btn-success dropdown-toggle">
		Hide
		<img src="./img/filter.svg" style="width: 30px ; height: 30px;">
	</button>	
	<div class="container" id="filters">
		<br>
		<a href="records.php?sort=fname"><button type="button" class="btn btn-warning">First Name</button></a>
		<a href="records.php?sort=lname"><button type="button" class="btn btn-warning">Last Name</button></a>
		<a href="records.php?sort=datejoined"><button type="button" class="btn btn-warning">Date</button></a>
		<a href="records.php?sort=regno"><button type="button" class="btn btn-warning">Registration No</button></a>
		<a href="records.php?sort=diagnosis"><button type="button" class="btn btn-warning">Clinical Diagnosis</button></a>
	</div>
	<br>
	<?php 
	# Check if sort paramenter is present in url
		$sort = '';
		$page = '1';
		if (isset($_GET['sort'])){
			$sort = $_GET['sort'];
		}
		if (isset($_GET['page'])){
			$page = $_GET['page'];
		}
		$page = (int)$page - 1;
		$offset = $page * 20;

		# Set the sql statement according to the sort variable filter
		if ($sort=='regno'){ $sql = "SELECT * FROM patient_data ORDER BY regno LIMIT " . $offset . ",20;"; }
		elseif ($sort=='fname'){ $sql = "SELECT * FROM patient_data ORDER BY firstName LIMIT " . $offset . ",20;"; }
		elseif ($sort=='lname'){ $sql = "SELECT * FROM patient_data ORDER BY lastName LIMIT " . $offset . ",20;"; } 
		elseif ($sort=='datejoined'){ $sql = "SELECT * FROM patient_data ORDER BY dateJoined LIMIT " . $offset . ",20;"; }
		elseif ($sort=='diagnosis'){ $sql = "SELECT * FROM patient_data ORDER BY diagnosis LIMIT " . $offset . ",20;"; }
		else{ $sql = "SELECT * FROM patient_data LIMIT " . $offset . ",20;"; }

		//echo $sql;
		?>

</div>
<!-- SORT FILTER DIV END -->

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<script>
	$(document).ready(function(){
		$("#showBtn").click(function(){
			$("#filters").show();
		});
	});
	$(document).ready(function(){
		$("#hideBtn").click(function(){
			$("#filters").hide();
		});
	});
</script>

<style>
	#filters{
		display: none;
	}
</style>