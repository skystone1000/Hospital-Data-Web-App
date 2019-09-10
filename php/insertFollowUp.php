<?php
include './includes/connection.php';

$followUpId = '';
$id	= $_GET['id'];
$regno = $_GET['regno'];
$weight = $_GET['weight'];

$treatment_output = $_GET['treatment_output'];
$other_complains = $_GET['other_complains'];
$treatment = $_GET['treatment'];
$medicine_duration = $_GET['medicine_duration'];
$paid = $_GET['paid'];
$balance = $_GET['balance'];

$sql_0 = "SELECT * FROM follow_up_data WHERE id LIKE '%$id%';";
$result = mysqli_query($conn, $sql_0);
$resultNum = mysqli_num_rows($result);
$last_follow_up = 0;

if($resultNum > 0){
	while($row = mysqli_fetch_assoc($result)){		
		if ($last_follow_up < $row['follow_up_num']){
			$last_follow_up = $row['follow_up_num'];
		}
	}
}

$follow_up_num = $last_follow_up + 1;
echo $follow_up_num;

$sql = "INSERT INTO follow_up_data (	followUpId,	id,	regno,	follow_up_num,	weight,	treatment_output,	other_complains,	treatment,	medicine_duration,	paid,	balance) VALUES (	'$followUpId',	'$id',	'$regno',	'$follow_up_num',	'$weight',	'$treatment_output',	'$other_complains',	'$treatment',	'$medicine_duration',	'$paid',	'$balance');";


mysqli_query($conn , $sql);
header("Location: ./records.php?insert=success");


?>