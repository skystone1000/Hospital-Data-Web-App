<?php
    $rid   = (int)$row['id'];
    $name  = h($row['regno']) . " : " . h($row['firstName']) . " " . h($row['middleName']) . " " . h($row['lastName']);
    echo '
    <div class="list-group-item list-group-item-action flex-column align-items-start active">
        <div class="d-flex justify-content-between row">
            <h5 class="mb-1 col-6">' . $name . '</h5>
            <div class="col"><a href="followUp.php?id=' . $rid . '" target="_blank"><button class="btn btn-warning my-2 my-sm-0" type="button">FollowUp</button></a></div>
            <div class="col"><a href="patientDetails.php?id=' . $rid . '"><button class="btn btn-success my-2 my-sm-0" type="button">Details</button></a></div>
            <div class="col">
                <form method="post" action="./php/deleteRecord.php" style="display:inline" onsubmit="return confirm(\'Are you sure you want to delete this patient?\');">
                    <input type="hidden" name="id" value="' . $rid . '">
                    <button class="btn btn-danger my-2 my-sm-0" type="submit">Delete</button>
                </form>
            </div>
        </div>
    </div>
    ';
?>
