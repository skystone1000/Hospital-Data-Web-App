<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "hospital";


// Create connection
$conn = new mysqli($servername, $username, $password);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
} 

// Create database
/*
$sql = "CREATE DATABASE hospital";
if ($conn->query($sql) === TRUE) {
    echo "Database created successfully";
} else {
    echo "Error creating database: " . $conn->error;
}
*/

// sql to create table
$sql = "CREATE TABLE patient_data (
id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY, 
firstname VARCHAR(30) NOT NULL,
lastname VARCHAR(30) NOT NULL,
email VARCHAR(50),
reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

id ,
firstName,	
middleName,	
lastName,	
age,	
sex,	
occupation,	
address,	
phone,	
regno,	
cc1,	
cc2,	
cc3,	
appetite,	
desire,	
aversions,	
thirst,	
perspiration,	
sleep,	
stool,	
urine,	
menses,	
thermal,	
mind,	
hobbies,	
particulars,	
on_examination,	
path_inv,	
previous_rx,	
past_history,	
family_history,	
treatment,	
followUp1,	
followUp2,	
followUp3,	
followUp4



)";

if ($conn->query($sql) === TRUE) {
    echo "Table MyGuests created successfully";
} else {
    echo "Error creating table: " . $conn->error;
}



$conn->close();
?>