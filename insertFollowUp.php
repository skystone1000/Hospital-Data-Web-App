<?php
include './headFoot/connection.php';

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

$sql = "INSERT INTO follow_up_data (	followUpId,	id,	regno,	weight,	treatment_output,	other_complains,	treatment,	medicine_duration,	paid,	balance) VALUES (	'$followUpId',	'$id',	'$regno',	'$weight',	'$treatment_output',	'$other_complains',	'$treatment',	'$medicine_duration',	'$paid',	'$balance');";


mysqli_query($conn , $sql);
header("Location: ./records.php?insert=success");


?>