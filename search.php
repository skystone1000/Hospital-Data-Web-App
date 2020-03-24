<?php
	include './includes/header.php';	
?>

<div class="container">
	<div class="list-group">
		
<?php

/*
$search = $_GET['dataSearch'];
echo "<pre>";
$searchE = explode(" ", $search);

$x = 0;
$construct = "";
foreach ($searchE as $term) {
	$x++;
	if ($x == 1) {
		$construct .= "firstName LIKE '%$term%' OR lastName LIKE '%$term%' OR regno LIKE '%$term%'";
	} else {
		$construct .= " OR firstName LIKE '%$term%' OR lastName LIKE '%$term%' OR regno LIKE '%$term%'";
	}
}

$pdo = new PDO('mysql:host=localhost;dbname=hospital','root','');
$results = $pdo->query("SELECT * FROM patient_data WHERE $construct;");
print_r($results->fetchAll());
*/

include './includes/connection.php';

$search = $_GET['dataSearch']; 
$searchE = explode(" ", $search);

$x = 0;
$construct = "";
foreach ($searchE as $term) {
	$x++;
	if ($x == 1) {
		$construct .= "firstName LIKE '%$term%' OR lastName LIKE '%$term%' OR regno LIKE '%$term%'";
	} else {
		$construct .= " OR firstName LIKE '%$term%' OR lastName LIKE '%$term%' OR regno LIKE '%$term%'";
	}
}


$sql = "SELECT * FROM patient_data WHERE $construct LIMIT 20;";
$result = mysqli_query($conn, $sql);
$resultNum = mysqli_num_rows($result);

if ($resultNum > 0) {
	echo "<br><h2>Search Results : Top " . $resultNum . " records ... </h2><br>";
	include './includes/detailsCard.php';
	while($row = mysqli_fetch_assoc($result)){
		include './includes/recordCard.php';
	}
} else {
	echo "<h1>No Results Found !!!</h1>";
}

?>

</div>

<?php
	include './includes/footer.php';
?>