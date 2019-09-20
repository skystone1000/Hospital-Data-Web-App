<?php
include './includes/header.php';
?>

<?php

$id = $_GET['id'];
include './includes/connection.php';

$sql = "SELECT * FROM patient_data WHERE id LIKE '%$id%';";
$result = mysqli_query($conn, $sql);
$data = mysqli_fetch_assoc($result);

?>


<div class="row">
    <div class="col-2" >
        <?php include './includes/fillFormSideNav.php' ?>
    </div>

    <div class="col-10">
        <div class="container">
           <br>
           <div class="row">
            <div class="col-md-6 mb-4">
                <h2 id="pd">Patient Details</h2>
            </div>
            <div class="col-md-2 mb-4">
                <?php
                echo '
                <div class="col"><a href="followUp.php?id=' . $id . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">FollowUp</button></a></div>
                '
                ?>
            </div>
            <div class="col-md-2 mb-4">
                <?php
                echo '
                <small><a href="patientDetailsEdit.php?id=' . $id . '"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Edit Record</button></a></small>
                ';
                ?>
            </div>
            <div class="col-md-2 mb-4">
                <?php
                echo '
                <div class="col"><a href="./php/deleteRecord.php?id=' . $id . '" onclick="return confirm(\'Are you sure you want to delete this item?\');"><button class="btn btn-success my-2 my-sm-0" type="submit" value="submit">Delete</button></a></div>
                ';                
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

            <div class="form-row">
                <div class="col-md-4 mb-3">
                    <label>Height : </label>
                    <input type="text" class="form-control" id="height" name="height" value="<?php echo $data['height'] ?>" readonly>
                </div>
                <div class="col-md-4 mb-3">
                    <label>Weight : </label>
                    <input type="text" class="form-control" id="weight" name="weight" value="<?php echo $data['weight'] ?>" readonly>
                </div> 
                <div class="col-md-4 mb-3">
                    <label>Clinical Diagnosis: </label>
                    <input type="text" class="form-control" id="diagnosis" name="diagnosis" value="<?php echo $data['diagnosis'] ?>" readonly>
                </div>
            </div>

            <br>
            <h3 id="cc0">Chief Complaints</h3>
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
                <label id="appetite">Appetite : - </label>
                <textarea type="text" class="form-control" rows="2" name="appetite" readonly><?php echo $data['appetite'] ?></textarea>
            </div>


            <div class="form-group">
                <label id="desire">Desire : - </label>
                <textarea class="form-control" rows="5" name="desire" readonly><?php echo $data['desire'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="aversions">Aversions : - </label>
                <textarea class="form-control" rows="2" name="aversions" readonly><?php echo $data['aversions'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="thirst">Thirst : - </label>
                <textarea class="form-control" rows="2" name="thirst" readonly><?php echo $data['thirst'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="perspiration">Perspiration : - </label>
                <textarea class="form-control" rows="2" name="perspiration" readonly><?php echo $data['perspiration'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="sleep">Sleep : - </label>
                <textarea class="form-control" rows="2" name="sleep" readonly><?php echo $data['sleep'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="stool">Stool : - </label>
                <textarea class="form-control" rows="2" name="stool" readonly><?php echo $data['stool'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="urine">Urine : - </label>
                <textarea class="form-control" rows="2" name="urine" readonly><?php echo $data['urine'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="menses">Menses : - </label>
                <textarea class="form-control" rows="2" name="menses" readonly><?php echo $data['menses'] ?></textarea>
            </div>


            <div class="form-group">
                <label id="thermal">Thermal : - </label>
                <textarea class="form-control" rows="1" name="thermal" readonly><?php echo $data['thermal'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="mind">Mind : - </label>
                <textarea class="form-control" rows="6" name="mind" readonly><?php echo $data['mind'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="hobbies">Hobbies : - </label>
                <textarea class="form-control" rows="2" name="hobbies" readonly><?php echo $data['hobbies'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="particulars">Particulars : - </label>
                <textarea class="form-control" rows="8" name="particulars" readonly><?php echo $data['particulars'] ?></textarea>
            </div>


            <div class="form-group">
                <label id="on_examination">On-Examination : - </label>
                <textarea class="form-control" rows="4" name="on_examination" readonly><?php echo $data['on_examination'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="path_inv">Path-Inv : - </label>
                <textarea class="form-control" rows="4" name="path_inv" readonly><?php echo $data['path_inv'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="previous_rx">Previous-Rx : - </label>
                <textarea class="form-control" rows="4" name="previous_rx" readonly><?php echo $data['previous_rx'] ?></textarea>
            </div>


            <div class="form-group">
                <label id="past_history">Past-history : - </label>
                <textarea class="form-control" rows="4" name="past_history" readonly><?php echo $data['past_history'] ?></textarea>
            </div>


            <div class="form-group">
                <label id="family_history">Family-history : - </label>
                <textarea class="form-control" rows="2" name="family_history" readonly><?php echo $data['family_history'] ?></textarea>
            </div>

            <div class="form-group">
                <label id="treatment">Treatment : - </label>
                <textarea class="form-control" rows="3" name="treatment" readonly><?php echo $data['treatment'] ?></textarea>
            </div>

            <div class="form-row">
                <div class="col-md-4 mb-3">
                    <label id="payment">Paid</label>
                    <input type="text" id="paid" class="form-control" name="paid" value="<?php echo $data['paid'] ?>" readonly>
                </div>
                <div class="col-md-4 mb-3">
                    <label>Balance</label>
                    <input type="text" id="balance" class="form-control" name="balance" value="<?php echo $data['balance'] ?>" readonly>
                </div>
            </div> 
            <br>



            <div class="form-group">
                <label>Follow Up 1 : - </label>
                <textarea class="form-control" rows="2" name="followUp1" readonly><?php echo $data['followUp1'] ?></textarea>
            </div>

            <div class="form-group">
                <label>Follow Up 2 : - </label>
                <textarea class="form-control" rows="2" name="followUp2" readonly><?php echo $data['followUp2'] ?></textarea>
            </div>

            <div class="form-group">
                <label>Follow Up 3 : - </label>
                <textarea class="form-control" rows="2" name="followUp3" readonly><?php echo $data['followUp3'] ?></textarea>
            </div>

            <div class="form-group">
                <label>Follow Up 4 : - </label>
                <textarea class="form-control" rows="2" name="followUp4" readonly><?php echo $data['followUp4'] ?></textarea>
            </div>

        </form>


    </div>
</div>

<?php
include './includes/footer.php';
?>