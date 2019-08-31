<?php
//include './headFoot/connection.php';
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "hospital";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}


$id	= $_GET['id'];
/*
$firstName	= $_GET['firstName'];
$middleName	= $_GET['middleName'];
$lastName	= $_GET['lastName'];
$age	= $_GET['age'];
$sex	= $_GET['sex'];
$occupation	= $_GET['occupation'];
$address	= $_GET['address'];
$phone	= $_GET['phone'];
$regno  = $_GET['regno'];
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
$followUp1	= $_GET['followUp1'];
$followUp2  = $_GET['followUp2'];
$followUp3	= $_GET['followUp3'];
$followUp4  = $_GET['followUp4'];
*/

// $sql = "UPDATE patient_data SET firstName='$firstName' ,	middleName = '$middleName',	lastName = '$lastName',	age = '$age',	sex = '$sex',	occupation = '$occupation',	address = '$address',	phone = '$phone',	regno = '$regno',	cc1 = '$cc1',	cc2 = '$cc2',	cc3 = '$cc3',	appetite = '$appetite',	desire = '$desire',	aversions = '$aversions',	thirst = '$thirst',	perspiration = '$perspiration',	sleep = '$sleep',	stool = '$stool',	urine = '$urine',	menses = '$menses',	thermal = '$thermal',	mind = '$mind',	hobbies = '$hobbies',	particulars = '$particulars',	on_examination = '$on_examination',	path_inv = '$path_inv',	previous_rx = '$previous_rx',	past_history = '$past_history',	family_history = '$family_history',	treatment = '$treatment',	followUp1 = '$followUp1',	followUp2 = '$followUp2',	followUp3 = '$followUp3',	followUp4 = '$followUp4' WHERE id = $id "; 

$sql = "DELETE FROM patient_data WHERE id = $id;";

mysqli_query($conn , $sql);
header("Location: ./records.php?id=" . $id . "&delete=success");

?>