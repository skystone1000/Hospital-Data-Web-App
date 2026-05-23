<?php
session_start();
if (!isset($_SESSION['adminId'])) {
    http_response_code(403);
    exit('Unauthorized');
}

include '../includes/connection.php';

$id                = filter_var($_POST['id'] ?? 0, FILTER_VALIDATE_INT);
$regno             = $_POST['regno']             ?? '';
$weight            = $_POST['weight']            ?? '';
$treatment_output  = $_POST['treatment_output']  ?? '';
$other_complains   = $_POST['other_complains']   ?? '';
$treatment         = $_POST['treatment']         ?? '';
$medicine_duration = $_POST['medicine_duration'] ?? '';
$paid              = $_POST['paid']              ?? '';
$balance           = $_POST['balance']           ?? '';

if (!$id) {
    header("Location: ../records.php?error=invalid_id");
    exit();
}

// Get current max follow_up_num using CAST to avoid VARCHAR ordering bug
$stmt_max = $conn->prepare(
    "SELECT MAX(CAST(follow_up_num AS UNSIGNED)) AS max_num FROM follow_up_data WHERE id = ?"
);
$stmt_max->bind_param("i", $id);
$stmt_max->execute();
$max_num       = $stmt_max->get_result()->fetch_assoc()['max_num'] ?? 0;
$follow_up_num = (int)$max_num + 1;

$stmt = $conn->prepare(
    "INSERT INTO follow_up_data
     (id, regno, follow_up_num, weight, treatment_output, other_complains,
      treatment, medicine_duration, paid, balance)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
);
$stmt->bind_param(
    "iissssssss",
    $id, $regno, $follow_up_num, $weight, $treatment_output,
    $other_complains, $treatment, $medicine_duration, $paid, $balance
);

if ($stmt->execute()) {
    header("Location: ../patientDetails.php?id=" . $id . "&insert=success");
} else {
    error_log("insertFollowUp failed: " . $conn->error);
    header("Location: ../patientDetails.php?id=" . $id . "&error=insert_failed");
}
exit();
