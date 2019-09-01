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

<div class="container">
	<br>
    <div class="row">
        <div class="col-md-4 mb-3">
    	   <h2>Patient Details</h2>
        </div>
        <div class="col-md-4 mb-3">

        </div>
        <div class="col-md-4 mb-3">
            
        </div>
    </div>



	<form action="updateRecord.php" method="get" class="container">
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>First name</label>
                <input type="text" class="form-control disabled" name="firstName" value="<?php echo $data['firstName'] ?>" >
            </div>
            <div class="col-md-4 mb-3">
                <label>Middle name</label>
                <input type="text" class="form-control" name="middleName" value="<?php echo $data['middleName'] ?>" >
            </div>
            <div class="col-md-4 mb-3">
                <label>Last name</label>
                <input type="text" class="form-control" name="lastName" value="<?php echo $data['lastName'] ?>" >
            </div>
        </div>
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Age</label>
                <input type="text" class="form-control" name="age" value="<?php echo $data['age'] ?>" >
            </div>
            <div class="col-md-4 mb-3">
                <label>Sex</label>
                <input type="text" class="form-control" name="sex" value="<?php echo $data['sex'] ?>" >
            </div>
            <div class="col-md-4 mb-3">
                <label>Occupation</label>
                <input type="text" class="form-control" name="occupation" value="<?php echo $data['occupation'] ?>" >
            </div>
        </div> 
        <br>

        <div class="form-group">
                <label>Address : </label>
                <input type="text" class="form-control input-lg" name="address" value="<?php echo $data['address'] ?>" >
        </div> <br>

        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Phone Number : </label>
                <input type="text" class="form-control" id="phone" name="phone" value="<?php echo $data['phone'] ?>" >
            </div> 
            <div class="col-md-4 mb-3">
                <label>Registration Number : </label>
                <input type="text" class="form-control" id="regno" name="regno" value="<?php echo $data['regno'] ?>" >
            </div>
            <div class="col-md-4 mb-3">
                <label>Id : </label>
                <input type="text" class="form-control" id="id" name="id" value="<?php echo $data['id'] ?>" >
            </div>  
        </div>

        <br>
        <h3>Chief Complaints</h3>
        <div class="form-group">
            <label>CC 1 : - </label>
            <input type="text" class="form-control" name="cc1" value="<?php echo $data['cc1'] ?>" >
        </div>
        
        <div class="form-group">
            <label>CC 2 : - </label>
            <input type="text" class="form-control" name="cc2" value="<?php echo $data['cc2'] ?>" >
        </div>
        
        <div class="form-group">
            <label>CC 3 : - </label>
            <input type="text" class="form-control" name="cc3" value="<?php echo $data['cc3'] ?>" >
        </div>

        <div class="form-group">
            <label>Appetite : - </label>
            <textarea class="form-control" rows="2" name="appetite" ><?php echo $data['appetite'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Desire : - </label>
            <textarea class="form-control" rows="5" name="desire" ><?php echo $data['desire'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Aversions : - </label>
            <textarea class="form-control" rows="2" name="aversions" ><?php echo $data['aversions'] ?></textarea>
        </div>
        
        <div class="form-group">
            <label>Thirst : - </label>
            <textarea class="form-control" rows="2" name="thirst" ><?php echo $data['thirst'] ?></textarea>
        </div>
        
        <div class="form-group">
            <label>Perspiration : - </label>
            <textarea class="form-control" rows="2" name="perspiration" ><?php echo $data['perspiration'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Sleep : - </label>
            <textarea class="form-control" rows="2" name="sleep" ><?php echo $data['sleep'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Stool : - </label>
            <textarea class="form-control" rows="2" name="stool" ><?php echo $data['stool'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Urine : - </label>
            <textarea class="form-control" rows="2" name="urine" ><?php echo $data['urine'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Menses : - </label>
            <textarea class="form-control" rows="2" name="menses" ><?php echo $data['menses'] ?></textarea>
        </div>
        

        <div class="form-group">
            <label>Thermal : - </label>
            <textarea class="form-control" rows="1" name="thermal" ><?php echo $data['thermal'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Mind : - </label>
            <textarea class="form-control" rows="10" name="mind" ><?php echo $data['mind'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Hobbies : - </label>
            <textarea class="form-control" rows="5" name="hobbies" ><?php echo $data['hobbies'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Particulars : - </label>
            <textarea class="form-control" rows="15" name="particulars" ><?php echo $data['particulars'] ?></textarea>
        </div>


        <div class="form-group">
            <label>On-Examination : - </label>
            <textarea class="form-control" rows="8" name="on_examination" ><?php echo $data['on_examination'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Path-Inv : - </label>
            <textarea class="form-control" rows="8" name="path_inv" ><?php echo $data['path_inv'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Previous-Rx : - </label>
            <textarea class="form-control" rows="8" name="previous_rx" ><?php echo $data['previous_rx'] ?></textarea>
        </div>


        <div class="form-group">
            <label>Past-history : - </label>
            <textarea class="form-control" rows="8" name="past_history" ><?php echo $data['past_history'] ?></textarea>
        </div>


        <div class="form-group">
            <label>Family-history : - </label>
            <textarea class="form-control" rows="2" name="family_history" ><?php echo $data['family_history'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Treatment : - </label>
            <textarea class="form-control" rows="3" name="treatment" ><?php echo $data['treatment'] ?></textarea>
        </div>


        <div class="form-group">
            <label>Follow Up 1 : - </label>
            <textarea class="form-control" rows="2" name="followUp1" ><?php echo $data['followUp1'] ?></textarea>
        </div>

        <div class="form-group">
            <label>Follow Up 2 : - </label>
            <textarea class="form-control" rows="2" name="followUp2" ><?php echo $data['followUp2'] ?></textarea>
        </div>
        
        <div class="form-group">
            <label>Follow Up 3 : - </label>
            <textarea class="form-control" rows="2" name="followUp3" ><?php echo $data['followUp3'] ?></textarea>
        </div>
        
        <div class="form-group">
            <label>Follow Up 4 : - </label>
            <textarea class="form-control" rows="2" name="followUp4" ><?php echo $data['followUp4'] ?></textarea>
        </div>

        <button class="btn btn-primary" type="submit">Update Record</button>
        <br>
        <br>

    </form>

</div>


<?php
    include './headFoot/footer.php';
?>