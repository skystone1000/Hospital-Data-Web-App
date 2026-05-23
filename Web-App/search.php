<?php
    include './includes/header.php';
?>

<div class="container">
    <div class="list-group">

<?php

include './includes/connection.php';

$search = trim($_GET['dataSearch'] ?? '');

if ($search === '') {
    echo "<h1>Please enter a search term.</h1>";
} else {
    $term = "%" . $search . "%";
    $stmt = $conn->prepare(
        "SELECT * FROM patient_data
         WHERE firstName LIKE ? OR lastName LIKE ? OR regno LIKE ?
         LIMIT 20"
    );
    $stmt->bind_param("sss", $term, $term, $term);
    $stmt->execute();
    $result    = $stmt->get_result();
    $resultNum = $result->num_rows;

    if ($resultNum > 0) {
        echo "<br><h2>Search Results : Top " . $resultNum . " records ... </h2><br>";
        include './includes/detailsCard.php';
        while ($row = $result->fetch_assoc()) {
            include './includes/recordCard.php';
        }
    } else {
        echo "<h1>No Results Found !!!</h1>";
    }
}

?>

</div>

<?php
    include './includes/footer.php';
?>
