<?php
	
	function calculate($conn, $sql){
		$result = mysqli_query($conn, $sql);
		$data = mysqli_fetch_assoc($result);
		return $data['count'];
	}

	function calcEarning($conn, $new, $follow){
		$newResult = mysqli_query($conn, $new);
		$newData = mysqli_fetch_assoc($newResult);
		$newTotal = $newData['count'];	
		//echo $newTotal . "  ";

		$followResult = mysqli_query($conn, $follow);
		$followData = mysqli_fetch_assoc($followResult);
		$followTotal = $followData['count'];
		//echo $followTotal . "  ";

		$totalEarn = $newTotal + $followTotal;
		return $totalEarn;
	}

	// Today
	$todayNew = "select COUNT(id) as count from patient_data where `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 0 DAY);";
	$todayNewCount = calculate($conn,$todayNew);

	$todayFollow = "select COUNT(id) as count from follow_up_data where `date` >= DATE_SUB(CURDATE(), INTERVAL 0 DAY);";
	$todayFollowCount = calculate($conn,$todayFollow);

	$todayEarnNew = "select SUM(paid) as count from patient_data where `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 0 DAY);";
	$todayEarnFollow = "select SUM(paid) as count from follow_up_data where `date` >= DATE_SUB(CURDATE(), INTERVAL 0 DAY);";
	$todayEarnCount = calcEarning($conn, $todayEarnNew, $todayEarnFollow);				

	// Week
	$weekNew = "select COUNT(id) as count from patient_data where  `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);";
	$weekNewCount = calculate($conn,$weekNew);

	$weekFollow = "select COUNT(id) as count from follow_up_data where  `date` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);";
	$weekFollowCount = calculate($conn,$weekFollow);

	$weekEarnNew = "select SUM(paid) as count from patient_data where `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);";
	$weekEarnFollow = "select SUM(paid) as count from follow_up_data where `date` >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);";
	$weekEarnCount = calcEarning($conn, $weekEarnNew, $weekEarnFollow);				

	// Month
	$monthNew = "select COUNT(id) as count from patient_data where  `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);";
	$monthNewCount = calculate($conn,$monthNew);

	$monthFollow = "select COUNT(id) as count from follow_up_data where  `date` >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);";
	$monthFollowCount = calculate($conn,$monthFollow);

	$monthEarnNew = "select SUM(paid) as count from patient_data where `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);";
	$monthEarnFollow = "select SUM(paid) as count from follow_up_data where `date` >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);";
	$monthEarnCount = calcEarning($conn, $monthEarnNew, $monthEarnFollow);

	// Year
	$yearNew = "select COUNT(id) as count from patient_data where  `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 365 DAY);";
	$yearNewCount = calculate($conn,$yearNew);

	$yearFollow = "select COUNT(id) as count from follow_up_data where  `date` >= DATE_SUB(CURDATE(), INTERVAL 365 DAY);";
	$yearFollowCount = calculate($conn,$yearFollow);

	$yearEarnNew = "select SUM(paid) as count from patient_data where `dateJoined` >= DATE_SUB(CURDATE(), INTERVAL 365 DAY);";
	$yearEarnFollow = "select SUM(paid) as count from follow_up_data where `date` >= DATE_SUB(CURDATE(), INTERVAL 365 DAY);";
	$yearEarnCount = calcEarning($conn, $yearEarnNew, $yearEarnFollow);
?>