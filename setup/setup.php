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

echo 'Creating Database ' . $dbname ;
// Create database
$sql = "CREATE DATABASE hospital";
if ($conn->query($sql) === TRUE) {
    echo "Database " . $dbname . " created successfully";
} else {
    echo "Error creating database: " . $conn->error;
}

echo 'Creating new connection with databse added.'
// Creating new conn variable
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

echo 'Creting Table patient_data'
// sql to create table
$sql = "CREATE TABLE `hospital`.`patient_data` ( `id` INT(5) NOT NULL AUTO_INCREMENT , `firstName` VARCHAR(20) NOT NULL , `middleName` VARCHAR(20) NOT NULL , `lastName` VARCHAR(20) NOT NULL , `age` INT(4) NOT NULL , `sex` VARCHAR(10) NOT NULL , `occupation` VARCHAR(20) NOT NULL , `address` VARCHAR(50) NOT NULL , `phone` VARCHAR(10) NOT NULL , `regno` VARCHAR(5) NOT NULL , `cc1` VARCHAR(150) NULL , `cc2` VARCHAR(150) NULL , `cc3` VARCHAR(150) NULL , `appetite` VARCHAR(100) NULL , `desire` VARCHAR(200) NULL , `aversions` VARCHAR(150) NULL , `thirst` VARCHAR(100) NULL , `perspiration` VARCHAR(100) NULL , `sleep` VARCHAR(200) NULL , `stool` VARCHAR(150) NULL , `urine` VARCHAR(150) NULL , `menses` VARCHAR(150) NULL , `thermal` VARCHAR(100) NULL , `mind` VARCHAR(800) NULL , `hobbies` VARCHAR(200) NULL , `particulars` VARCHAR(800) NULL , `on_examination` VARCHAR(200) NULL , `path_inv` VARCHAR(200) NULL , `previous_rx` VARCHAR(200) NULL , `past_history` VARCHAR(150) NULL , `family_history` VARCHAR(200) NULL , `treatment` VARCHAR(200) NULL , `followUp1` VARCHAR(400) NULL , `followUp2` VARCHAR(400) NULL , `followUp3` VARCHAR(400) NULL , `followUp4` VARCHAR(400) NULL , `dateJoined` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`id`)) ENGINE = InnoDB;
	
	";

//ALTER TABLE patient_data MODIFY phone VARCHAR(10);

if ($conn->query($sql) === TRUE) {
    echo "Table patient_data created successfully";
} else {
    echo "Error creating table: " . $conn->error;
}



$conn->close();
?>