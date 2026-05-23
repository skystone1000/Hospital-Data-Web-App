<?php
session_start();
if (!isset($_SESSION['adminId'])) {
    http_response_code(403);
    exit('Unauthorized');
}

include '../includes/connection.php';

$id = filter_var($_POST['id'] ?? 0, FILTER_VALIDATE_INT);
if (!$id) {
    header("Location: ../records.php?error=invalid_id");
    exit();
}

// Delete follow-ups first, then patient (cascade)
$conn->begin_transaction();
try {
    $stmt1 = $conn->prepare("DELETE FROM follow_up_data WHERE id = ?");
    $stmt1->bind_param("i", $id);
    $stmt1->execute();

    $stmt2 = $conn->prepare("DELETE FROM patient_data WHERE id = ?");
    $stmt2->bind_param("i", $id);
    $stmt2->execute();

    $conn->commit();
    header("Location: ../records.php?delete=success");
} catch (Exception $e) {
    $conn->rollback();
    error_log("deleteRecord failed: " . $e->getMessage());
    header("Location: ../records.php?error=delete_failed");
}
exit();
