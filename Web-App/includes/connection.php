<?php
require_once __DIR__ . '/helpers.php';

$conn = new mysqli("localhost", "root", "", "hospital");
if ($conn->connect_error) {
    die("Connection failed.");
}
$conn->set_charset("utf8mb4");
