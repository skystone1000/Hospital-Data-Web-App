<?php
session_start();

if (!isset($_POST['login-submit'])) {
    header("Location: ../index.php");
    exit();
}

require 'connection.php';

$mailuid  = trim($_POST['mailuid'] ?? '');
$password = $_POST['pwd'] ?? '';

if (empty($mailuid) || empty($password)) {
    header("Location: ../index.php?error=emptyfields");
    exit();
}

$stmt = $conn->prepare("SELECT * FROM admin_users WHERE email_admin=? OR uid_admin=?");
if (!$stmt) {
    header("Location: ../index.php?error=sqlerror");
    exit();
}

$stmt->bind_param("ss", $mailuid, $mailuid);
$stmt->execute();
$row = $stmt->get_result()->fetch_assoc();

if (!$row) {
    header("Location: ../index.php?error=noUser");
    exit();
}

$matched = false;

if (password_verify($password, $row['password_admin'])) {
    $matched = true;
} elseif ($password === $row['password_admin']) {
    // Plaintext password in DB — hash it now and save
    $hash = password_hash($password, PASSWORD_BCRYPT);
    $upd  = $conn->prepare("UPDATE admin_users SET password_admin=? WHERE id_admin=?");
    $upd->bind_param("si", $hash, $row['id_admin']);
    $upd->execute();
    $matched = true;
}

if (!$matched) {
    header("Location: ../index.php?error=wrongPassword");
    exit();
}

$_SESSION['adminId']   = $row['id_admin'];
$_SESSION['adminUid']  = $row['uid_admin'];
$_SESSION['firstName'] = $row['firstName'];
$_SESSION['lastName']  = $row['lastName'];

header("Location: ../home.php");
exit();
