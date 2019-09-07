<?php
include './headFoot/connection.php';

$id	= $_GET['id'];
$firstName	= $_GET['firstName'];
$middleName	= $_GET['middleName'];
$lastName	= $_GET['lastName'];
$age	= $_GET['age'];
$sex	= $_GET['sex'];
$occupation	= $_GET['occupation'];
$address	= $_GET['address'];
$phone	= $_GET['phone'];
$regno  = $_GET['regno'];
$height = $_GET['height'];
$weight = $_GET['weight'];
$diagnosis = $_GET['diagnosis'];

$cc1	= $_GET['cc1'];
$cc2	= $_GET['cc2'];
$cc3	= $_GET['cc3'];
$appetite	= $_GET['appetite'];
$desire	= $_GET['desire'];
$aversions	= $_GET['aversions'];
$thirst	= $_GET['thirst'];
$perspiration	= $_GET['perspiration'];
$sleep	= $_GET['sleep'];
$stool	= $_GET['stool'];
$urine	= $_GET['urine'];
$menses	= $_GET['menses'];
$thermal	= $_GET['thermal'];
$mind	= $_GET['mind'];
$hobbies	= $_GET['hobbies'];
$particulars	= $_GET['particulars'];
$on_examination	= $_GET['on_examination'];
$path_inv	= $_GET['path_inv'];
$previous_rx	= $_GET['previous_rx'];
$past_history	= $_GET['past_history'];
$family_history	= $_GET['family_history'];
$treatment	= $_GET['treatment'];
$paid = $_GET['paid'];
$balance = $_GET['balance'];

$followUp1	= $_GET['followUp1'];
$followUp2  = $_GET['followUp2'];
$followUp3	= $_GET['followUp3'];
$followUp4  = $_GET['followUp4'];


$sql = "UPDATE patient_data SET firstName='$firstName' ,	middleName = '$middleName',	lastName = '$lastName',	age = '$age',	sex = '$sex',	occupation = '$occupation',	address = '$address',	phone = '$phone',	regno = '$regno',	height = '$height',	weight = '$weight',	diagnosis = '$diagnosis',	cc1 = '$cc1',	cc2 = '$cc2',	cc3 = '$cc3',	appetite = '$appetite',	desire = '$desire',	aversions = '$aversions',	thirst = '$thirst',	perspiration = '$perspiration',	sleep = '$sleep',	stool = '$stool',	urine = '$urine',	menses = '$menses',	thermal = '$thermal',	mind = '$mind',	hobbies = '$hobbies',	particulars = '$particulars',	on_examination = '$on_examination',	path_inv = '$path_inv',	previous_rx = '$previous_rx',	past_history = '$past_history',	family_history = '$family_history',	treatment = '$treatment',	paid = '$paid',	balance = '$balance',	followUp1 = '$followUp1',	followUp2 = '$followUp2',	followUp3 = '$followUp3',	followUp4 = '$followUp4' WHERE id = $id "; 


mysqli_query($conn , $sql);
header("Location: ./patientDetails.php?id=" . $id . "&insert=success");

?>