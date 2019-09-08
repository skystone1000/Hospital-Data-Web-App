<?php 
	include './headFoot/header.php';
?>

<?php

$id = $_GET['id'];
include './headFoot/connection.php';

$sql = "SELECT * FROM patient_data WHERE id LIKE '%$id%';";
$result = mysqli_query($conn, $sql);
$data = mysqli_fetch_assoc($result);

?>


<br>
    <div class="container">
        <h1> Patient Details - Add Follow Up<br>
        <small>Record Of :</small>
        </h1>
    </div>

    <form action="insertFollowUp.php" method="get" class="container" onsubmit="setFormSubmitting()">
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>First name</label>
                <input type="text" class="form-control disabled" name="firstName" value="<?php echo $data['firstName'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Last name</label>
                <input type="text" class="form-control" name="lastName" value="<?php echo $data['lastName'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Registration Number : </label>
                <input type="text" class="form-control" id="regno" name="regno" value="<?php echo $data['regno'] ?>" readonly>
            </div>
        </div>

        <h2>Follow Up Details : </h2>
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Weight : </label>
                <input type="text" class="form-control" id="weight" name="weight" placeholder="Weight">
            </div>
            <div class="col-md-4 mb-3">
            	<label>Previous treatment Output : </label>
            	<select class="form-control" name="treatment_output">
				    <option>None</option>
				    <option>Better</option>
				    <option>Same</option>
				    <option>Worse</option>
				</select>
            </div>
        </div>

        <div class="form-group">
            <label>Other Complains : - </label>
            <textarea class="form-control" rows="3" name="other_complains" placeholder="Other Complains"></textarea>
        </div>

        <div class="form-group">
            <label>Treatment : - </label>
            <textarea class="form-control" rows="3" name="treatment" placeholder="Treatment Given"></textarea>
        </div>

        <div class="form-row">
            <div class="col-md-4 mb-3">
            	<label>Medicine Duration : </label>
            	<select class="form-control" name="medicine_duration">
				    <option>None</option>
				    <option>3 Days</option>
				    <option>8 Days</option>
				    <option>15 Days</option>
				    <option>1 Month</option>
				</select>
            </div>
            <div class="col-md-4 mb-3">
                <label>Paid Amount</label>
                <input type="text" id="paid" class="form-control" name="paid" placeholder="Paid" required>
            </div>
            <div class="col-md-4 mb-3">
                <label>Balance Amount</label>
                <input type="text" id="balance" class="form-control" name="balance" placeholder="Balance"  required>
            </div>
        </div> 
        <br>

        <button id="submitBtn" class="btn btn-primary" type="submit">Add Follow Up</button>
    </form>
    <br><br><br>
</body>
<!--
<script src="//cdnjs.cloudflare.com/ajax/libs/annyang/2.6.1/annyang.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/speechkitt.min.js"></script>
-->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<script type="text/javascript" src="./js/script.js"></script>

<script>
    // To give a popup alert when user is switching to other page that 
    // unsaved data will be lost    
    var formSubmitting = false;
    var setFormSubmitting = function() { formSubmitting = true; };

    window.onload = function() {
        window.addEventListener("beforeunload", function (e) {
            if (formSubmitting) {
                return undefined;
            }

            var confirmationMessage = 'It looks like you have been editing something. '
                                    + 'If you leave before saving, your changes will be lost.';

            (e || window.event).returnValue = confirmationMessage; //Gecko + IE
            return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
        });
    };
</script>




<?php
	include './headFoot/footer.php';
?>