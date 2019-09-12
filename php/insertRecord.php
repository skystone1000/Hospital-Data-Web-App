<?php
include '../includes/connection.php';

$id	= '' ;
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


$sql = "INSERT INTO patient_data (	id,	firstName,	middleName,	lastName,	age,	sex,	occupation,	address,	phone,	regno,	height,	weight,	diagnosis,	cc1,	cc2,	cc3,	appetite,	desire,	aversions,	thirst,	perspiration,	sleep,	stool,	urine,	menses,	thermal,	mind,	hobbies,	particulars,	on_examination,	path_inv,	previous_rx,	past_history,	family_history,	treatment,	paid,	balance,	followUp1,	followUp2,	followUp3,	followUp4) VALUES (	'$id',	'$firstName',	'$middleName',	'$lastName',	'$age',	'$sex',	'$occupation',	'$address',	'$phone',	'$regno',	'$height',	'$weight',	'$diagnosis',	'$cc1',	'$cc2',	'$cc3',	'$appetite',	'$desire',	'$aversions',	'$thirst',	'$perspiration',	'$sleep',	'$stool',	'$urine',	'$menses',	'$thermal',	'$mind',	'$hobbies',	'$particulars',	'$on_examination',	'$path_inv',	'$previous_rx',	'$past_history',	'$family_history',	'$treatment',	'$paid',	'$balance',	'$followUp1',	'$followUp2',	'$followUp3',	'$followUp4');";


mysqli_query($conn , $sql);
header("Location: ../fillForm.php?insert=success");

?>