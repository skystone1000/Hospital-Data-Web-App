<?php

function calculate($conn, $sql) {
    $result = mysqli_query($conn, $sql);
    $data   = mysqli_fetch_assoc($result);
    return (int)$data['count'];
}

function calcEarning($conn, $new, $follow) {
    $newResult  = mysqli_query($conn, $new);
    $newData    = mysqli_fetch_assoc($newResult);
    $newTotal   = (float)$newData['count'];

    $followResult = mysqli_query($conn, $follow);
    $followData   = mysqli_fetch_assoc($followResult);
    $followTotal  = (float)$followData['count'];

    return $newTotal + $followTotal;
}

// Use CAST(paid AS DECIMAL) so non-numeric values are safely treated as 0
$sumExpr = "SUM(CAST(paid AS DECIMAL(10,2)))";

// Today
$todayNew      = "SELECT COUNT(id) as count FROM patient_data WHERE dateJoined >= CURDATE()";
$todayNewCount = calculate($conn, $todayNew);

$todayFollow      = "SELECT COUNT(id) as count FROM follow_up_data WHERE date >= CURDATE()";
$todayFollowCount = calculate($conn, $todayFollow);

$todayEarnNew    = "SELECT $sumExpr as count FROM patient_data WHERE dateJoined >= CURDATE()";
$todayEarnFollow = "SELECT $sumExpr as count FROM follow_up_data WHERE date >= CURDATE()";
$todayEarnCount  = calcEarning($conn, $todayEarnNew, $todayEarnFollow);

// Week
$weekNew      = "SELECT COUNT(id) as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
$weekNewCount = calculate($conn, $weekNew);

$weekFollow      = "SELECT COUNT(id) as count FROM follow_up_data WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
$weekFollowCount = calculate($conn, $weekFollow);

$weekEarnNew    = "SELECT $sumExpr as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
$weekEarnFollow = "SELECT $sumExpr as count FROM follow_up_data WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
$weekEarnCount  = calcEarning($conn, $weekEarnNew, $weekEarnFollow);

// Month
$monthNew      = "SELECT COUNT(id) as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
$monthNewCount = calculate($conn, $monthNew);

$monthFollow      = "SELECT COUNT(id) as count FROM follow_up_data WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
$monthFollowCount = calculate($conn, $monthFollow);

$monthEarnNew    = "SELECT $sumExpr as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
$monthEarnFollow = "SELECT $sumExpr as count FROM follow_up_data WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
$monthEarnCount  = calcEarning($conn, $monthEarnNew, $monthEarnFollow);

// Year
$yearNew      = "SELECT COUNT(id) as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)";
$yearNewCount = calculate($conn, $yearNew);

$yearFollow      = "SELECT COUNT(id) as count FROM follow_up_data WHERE date >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)";
$yearFollowCount = calculate($conn, $yearFollow);

$yearEarnNew    = "SELECT $sumExpr as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)";
$yearEarnFollow = "SELECT $sumExpr as count FROM follow_up_data WHERE date >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)";
$yearEarnCount  = calcEarning($conn, $yearEarnNew, $yearEarnFollow);
?>
