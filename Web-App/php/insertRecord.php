<?php
session_start();
if (!isset($_SESSION['adminId'])) {
    http_response_code(403);
    exit('Unauthorized');
}

include '../includes/connection.php';

$firstName      = trim($_POST['firstName']      ?? '');
$middleName     = trim($_POST['middleName']      ?? '');
$lastName       = trim($_POST['lastName']        ?? '');
$age            = filter_var($_POST['age'] ?? '', FILTER_VALIDATE_INT, ['options' => ['min_range' => 0, 'max_range' => 150]]);
$sex            = $_POST['sex']            ?? '';
$occupation     = $_POST['occupation']     ?? '';
$address        = $_POST['address']        ?? '';
$phone          = $_POST['phone']          ?? '';
$regno          = $_POST['regno']          ?? '';
$height         = (int)($_POST['height']   ?? 0);
$weight         = (int)($_POST['weight']   ?? 0);
$diagnosis      = $_POST['diagnosis']      ?? '';
$cc1            = $_POST['cc1']            ?? '';
$cc2            = $_POST['cc2']            ?? '';
$cc3            = $_POST['cc3']            ?? '';
$appetite       = $_POST['appetite']       ?? '';
$desire         = $_POST['desire']         ?? '';
$aversions      = $_POST['aversions']      ?? '';
$thirst         = $_POST['thirst']         ?? '';
$perspiration   = $_POST['perspiration']   ?? '';
$sleep          = $_POST['sleep']          ?? '';
$stool          = $_POST['stool']          ?? '';
$urine          = $_POST['urine']          ?? '';
$menses         = $_POST['menses']         ?? '';
$thermal        = $_POST['thermal']        ?? '';
$mind           = $_POST['mind']           ?? '';
$hobbies        = $_POST['hobbies']        ?? '';
$particulars    = $_POST['particulars']    ?? '';
$on_examination = $_POST['on_examination'] ?? '';
$path_inv       = $_POST['path_inv']       ?? '';
$previous_rx    = $_POST['previous_rx']    ?? '';
$past_history   = $_POST['past_history']   ?? '';
$family_history = $_POST['family_history'] ?? '';
$treatment      = $_POST['treatment']      ?? '';
$paid           = $_POST['paid']           ?? '';
$balance        = $_POST['balance']        ?? '';
$followUp1      = $_POST['followUp1']      ?? '';
$followUp2      = $_POST['followUp2']      ?? '';
$followUp3      = $_POST['followUp3']      ?? '';
$followUp4      = $_POST['followUp4']      ?? '';

if (empty($firstName) || empty($lastName) || $age === false) {
    header("Location: ../fillForm.php?error=invalid_input");
    exit();
}

$stmt = $conn->prepare(
    "INSERT INTO patient_data
     (firstName, middleName, lastName, age, sex, occupation, address, phone, regno,
      height, weight, diagnosis, cc1, cc2, cc3, appetite, desire, aversions, thirst,
      perspiration, sleep, stool, urine, menses, thermal, mind, hobbies, particulars,
      on_examination, path_inv, previous_rx, past_history, family_history, treatment,
      paid, balance, followUp1, followUp2, followUp3, followUp4)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
);

$stmt->bind_param(
    "sssisssssiissssssssssssssssssssssssssss",
    $firstName, $middleName, $lastName, $age, $sex, $occupation, $address, $phone, $regno,
    $height, $weight, $diagnosis, $cc1, $cc2, $cc3, $appetite, $desire, $aversions, $thirst,
    $perspiration, $sleep, $stool, $urine, $menses, $thermal, $mind, $hobbies, $particulars,
    $on_examination, $path_inv, $previous_rx, $past_history, $family_history, $treatment,
    $paid, $balance, $followUp1, $followUp2, $followUp3, $followUp4
);

if ($stmt->execute()) {
    header("Location: ../fillForm.php?insert=success");
} else {
    error_log("insertRecord failed: " . $conn->error);
    header("Location: ../fillForm.php?error=insert_failed");
}
exit();
