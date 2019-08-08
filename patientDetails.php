<?php
    include './headFoot/header.php';
?>

<?php
$id = $_GET['id'];
include './headFoot/connection.php';

//echo '<h1>' . $id . '</h1>';

$sql = "SELECT * FROM patient_data WHERE id LIKE '%$id%';";
$result = mysqli_query($conn, $sql);
$data = mysqli_fetch_assoc($result);

//echo $data['firstName'] ;

?>

<div class="container">
	<br>
    <div class="row">
        <div class="col-md-4 mb-3">
    	   <h2>Patient Details</h2>
        </div>
        <div class="col-md-4 mb-3">

        </div>
        <div class="col-md-4 mb-3">
            <?php
            echo '
            <small><a href="patientDetailsEdit.php?id=' . $id . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Edit Record</button></a></small>
            '
            ?>
        </div>
    </div>



	<form class="container">
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>First name</label>
                <input type="text" class="form-control disabled" name="firstName" value="<?php echo $data['firstName'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Middle name</label>
                <input type="text" class="form-control" name="middleName" value="<?php echo $data['middleName'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Last name</label>
                <input type="text" class="form-control" name="lastName" value="<?php echo $data['lastName'] ?>" readonly>
            </div>
        </div>
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Age</label>
                <input type="text" class="form-control" name="age" value="<?php echo $data['age'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Sex</label>
                <input type="text" class="form-control" name="sex" value="<?php echo $data['sex'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Occupation</label>
                <input type="text" class="form-control" name="occupation" value="<?php echo $data['occupation'] ?>" readonly>
            </div>
        </div> 
        <br>

        <div class="form-group">
                <label>Address : </label>
                <input type="text" class="form-control input-lg" name="address" value="<?php echo $data['address'] ?>" readonly>
        </div> <br>

        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Phone Number : </label>
                <input type="text" class="form-control" id="phone" name="phone" value="<?php echo $data['phone'] ?>" readonly>
            </div> 
            <div class="col-md-4 mb-3">
                <label>Registration Number : </label>
                <input type="text" class="form-control" id="regno" name="regno" value="<?php echo $data['regno'] ?>" readonly>
            </div>
            <div class="col-md-4 mb-3">
                <label>Id : </label>
                <input type="text" class="form-control" id="id" name="id" value="<?php echo $data['id'] ?>" readonly>
            </div> 
        </div>

        <br>
        <h3>Chief Complaints</h3>
        <div class="form-group">
            <label>CC 1 : - </label>
            <input type="text" class="form-control" name="cc1" value="<?php echo $data['cc1'] ?>" readonly>
        </div>
        
        <div class="form-group">
            <label>CC 2 : - </label>
            <input type="text" class="form-control" name="cc2" value="<?php echo $data['cc2'] ?>" readonly>
        </div>
        
        <div class="form-group">
            <label>CC 3 : - </label>
            <input type="text" class="form-control" name="cc3" value="<?php echo $data['cc3'] ?>" readonly>
        </div>

        <div class="form-group">
            <label>Appetite : - </label>
            <textarea class="form-control" rows="2" name="appetite"  value="<?php echo $data['appetite'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Desire : - </label>
            <textarea class="form-control" rows="5" name="desire" value="<?php echo $data['desire'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Aversions : - </label>
            <textarea class="form-control" rows="2" name="aversions"  value="<?php echo $data['aversions'] ?>" readonly></textarea>
        </div>
        
        <div class="form-group">
            <label>Thirst : - </label>
            <textarea class="form-control" rows="2" name="thirst" value="<?php echo $data['thirst'] ?>" readonly></textarea>
        </div>
        
        <div class="form-group">
            <label>Perspiration : - </label>
            <textarea class="form-control" rows="2" name="perspiration" value="<?php echo $data['perspiration'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Sleep : - </label>
            <textarea class="form-control" rows="2" name="sleep" value="<?php echo $data['sleep'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Stool : - </label>
            <textarea class="form-control" rows="2" name="stool" value="<?php echo $data['stool'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Urine : - </label>
            <textarea class="form-control" rows="2" name="urine" value="<?php echo $data['urine'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Menses : - </label>
            <textarea class="form-control" rows="2" name="menses" value="<?php echo $data['menses'] ?>" readonly></textarea>
        </div>
        

        <div class="form-group">
            <label>Thermal : - </label>
            <textarea class="form-control" rows="1" name="thermal" value="<?php echo $data['thermal'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Mind : - </label>
            <textarea class="form-control" rows="10" name="mind" value="<?php echo $data['mind'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Hobbies : - </label>
            <textarea class="form-control" rows="5" name="hobbies" value="<?php echo $data['hobbies'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Particulars : - </label>
            <textarea class="form-control" rows="15" name="particulars" value="<?php echo $data['particulars'] ?>" readonly></textarea>
        </div>


        <div class="form-group">
            <label>On-Examination : - </label>
            <textarea class="form-control" rows="8" name="on_examination" value="<?php echo $data['on_examination'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Path-Inv : - </label>
            <textarea class="form-control" rows="8" name="path_inv" value="<?php echo $data['path_inv'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Previous-Rx : - </label>
            <textarea class="form-control" rows="8" name="previous_rx" value="<?php echo $data['previous_rx'] ?>" readonly></textarea>
        </div>


        <div class="form-group">
            <label>Past-history : - </label>
            <textarea class="form-control" rows="8" name="past_history" value="<?php echo $data['past_history'] ?>" readonly></textarea>
        </div>


        <div class="form-group">
            <label>Family-history : - </label>
            <textarea class="form-control" rows="2" name="family_history" value="<?php echo $data['family_history'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Treatment : - </label>
            <textarea class="form-control" rows="3" name="treatment" value="<?php echo $data['treatment'] ?>" readonly></textarea>
        </div>


        <div class="form-group">
            <label>Follow Up 1 : - </label>
            <textarea class="form-control" rows="2" name="followUp1" value="<?php echo $data['followUp1'] ?>" readonly></textarea>
        </div>

        <div class="form-group">
            <label>Follow Up 2 : - </label>
            <textarea class="form-control" rows="2" name="followUp2" value="<?php echo $data['followUp2'] ?>" readonly></textarea>
        </div>
        
        <div class="form-group">
            <label>Follow Up 3 : - </label>
            <textarea class="form-control" rows="2" name="followUp3" value="<?php echo $data['followUp3'] ?>" readonly></textarea>
        </div>
        
        <div class="form-group">
            <label>Follow Up 4 : - </label>
            <textarea class="form-control" rows="2" name="followUp4" value="<?php echo $data['followUp4'] ?>" readonly></textarea>
        </div>

    </form>







</div>