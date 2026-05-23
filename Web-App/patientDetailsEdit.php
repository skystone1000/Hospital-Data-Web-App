<?php
include './includes/header.php';
?>

<?php
$id = filter_var($_GET['id'] ?? 0, FILTER_VALIDATE_INT);
include './includes/connection.php';
$stmt = $conn->prepare("SELECT * FROM patient_data WHERE id = ?");
$stmt->bind_param("i", $id);
$stmt->execute();
$data = $stmt->get_result()->fetch_assoc();
?>



<div class="row">
    <div class="col-2" >
        <?php include './includes/fillFormSideNav.php' ?>
    </div>

    <div class="col-10">
        <div class="container">
            <br>
            <div class="row">
                <div class="col-md-4 mb-3">
                    <h2 id="pd">Patient Details</h2>
                </div>
                <div class="col-md-4 mb-3">

                </div>
                <div class="col-md-4 mb-3">

                </div>
            </div>



            <form action="./php/updateRecord.php" method="post" class="container">
                <div class="form-row">
                    <div class="col-md-4 mb-3">
                        <label>First name</label>
                        <input type="text" class="form-control disabled" name="firstName" value="<?php echo h($data['firstName']) ?>" >
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Middle name</label>
                        <input type="text" class="form-control" name="middleName" value="<?php echo h($data['middleName']) ?>" >
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Last name</label>
                        <input type="text" class="form-control" name="lastName" value="<?php echo h($data['lastName']) ?>" >
                    </div>
                </div>
                <div class="form-row">
                    <div class="col-md-4 mb-3">
                        <label>Age</label>
                        <input type="text" class="form-control" name="age" value="<?php echo h($data['age']) ?>" >
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Sex</label>
                        <input type="text" class="form-control" name="sex" value="<?php echo h($data['sex']) ?>" >
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Occupation</label>
                        <input type="text" class="form-control" name="occupation" value="<?php echo h($data['occupation']) ?>" >
                    </div>
                </div> 
                <br>

                <div class="form-group">
                    <label>Address : </label>
                    <input type="text" class="form-control input-lg" name="address" value="<?php echo h($data['address']) ?>" >
                </div> <br>

                <div class="form-row">
                    <div class="col-md-4 mb-3">
                        <label>Phone Number : </label>
                        <input type="text" class="form-control" id="phone" name="phone" value="<?php echo h($data['phone']) ?>" maxlength="15">
                    </div> 
                    <div class="col-md-4 mb-3">
                        <label>Registration Number : </label>
                        <input type="text" class="form-control" id="regno" name="regno" value="<?php echo h($data['regno']) ?>" >
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Id : </label>
                        <input type="text" class="form-control" id="id" name="id" value="<?php echo h($data['id']) ?>" >
                    </div>  
                </div>

                <div class="form-row">
                    <div class="col-md-4 mb-3">
                        <label>Height : </label>
                        <input type="text" class="form-control" id="height" name="height" value="<?php echo h($data['height']) ?>" >
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Weight : </label>
                        <input type="text" class="form-control" id="weight" name="weight" value="<?php echo h($data['weight']) ?>" >
                    </div> 
                    <div class="col-md-4 mb-3">
                        <label>Clinical Diagnosis: </label>
                        <input type="text" class="form-control" id="diagnosis" name="diagnosis" value="<?php echo h($data['diagnosis']) ?>" >
                    </div>
                </div>


                <br>
                <h3 id="cc0">Chief Complaints</h3>
                <div class="form-group">
                    <label>CC 1 : - </label>
                    <input type="text" class="form-control" name="cc1" value="<?php echo h($data['cc1']) ?>" >
                </div>

                <div class="form-group">
                    <label>CC 2 : - </label>
                    <input type="text" class="form-control" name="cc2" value="<?php echo h($data['cc2']) ?>" >
                </div>

                <div class="form-group">
                    <label>CC 3 : - </label>
                    <input type="text" class="form-control" name="cc3" value="<?php echo h($data['cc3']) ?>" >
                </div>

                <div class="form-group">
                    <label id="appetite">Appetite : - </label>
                    <textarea class="form-control" rows="2" name="appetite" ><?php echo h($data['appetite']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="desire">Desire : - </label>
                    <textarea class="form-control" rows="5" name="desire" ><?php echo h($data['desire']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="aversions">Aversions : - </label>
                    <textarea class="form-control" rows="2" name="aversions" ><?php echo h($data['aversions']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="thirst">Thirst : - </label>
                    <textarea class="form-control" rows="2" name="thirst" ><?php echo h($data['thirst']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="perspiration">Perspiration : - </label>
                    <textarea class="form-control" rows="2" name="perspiration" ><?php echo h($data['perspiration']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="sleep">Sleep : - </label>
                    <textarea class="form-control" rows="2" name="sleep" ><?php echo h($data['sleep']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="stool">Stool : - </label>
                    <textarea class="form-control" rows="2" name="stool" ><?php echo h($data['stool']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="urine">Urine : - </label>
                    <textarea class="form-control" rows="2" name="urine" ><?php echo h($data['urine']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="menses">Menses : - </label>
                    <textarea class="form-control" rows="2" name="menses" ><?php echo h($data['menses']) ?></textarea>
                </div>


                <div class="form-group">
                    <label id="thermal">Thermal : - </label>
                    <textarea class="form-control" rows="1" name="thermal" ><?php echo h($data['thermal']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="mind">Mind : - </label>
                    <textarea class="form-control" rows="6" name="mind" ><?php echo h($data['mind']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="hobbies">Hobbies : - </label>
                    <textarea class="form-control" rows="2" name="hobbies" ><?php echo h($data['hobbies']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="particulars">Particulars : - </label>
                    <textarea class="form-control" rows="8" name="particulars" ><?php echo h($data['particulars']) ?></textarea>
                </div>


                <div class="form-group">
                    <label id="on_examination">On-Examination : - </label>
                    <textarea class="form-control" rows="4" name="on_examination" ><?php echo h($data['on_examination']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="path_inv">Path-Inv : - </label>
                    <textarea class="form-control" rows="4" name="path_inv" ><?php echo h($data['path_inv']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="previous_rx">Previous-Rx : - </label>
                    <textarea class="form-control" rows="4" name="previous_rx" ><?php echo h($data['previous_rx']) ?></textarea>
                </div>


                <div class="form-group">
                    <label id="past_history">Past-history : - </label>
                    <textarea class="form-control" rows="4" name="past_history" ><?php echo h($data['past_history']) ?></textarea>
                </div>


                <div class="form-group">
                    <label id="family_history">Family-history : - </label>
                    <textarea class="form-control" rows="2" name="family_history" ><?php echo h($data['family_history']) ?></textarea>
                </div>

                <div class="form-group">
                    <label id="family_history">Treatment : - </label>
                    <textarea class="form-control" rows="3" name="treatment" ><?php echo h($data['treatment']) ?></textarea>
                </div>

                <div class="form-row">
                    <div class="col-md-4 mb-3">
                        <label id="payment">Paid</label>
                        <input type="text" id="paid" class="form-control" name="paid" value="<?php echo h($data['paid']) ?>">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label>Balance</label>
                        <input type="text" id="balance" class="form-control" name="balance" value="<?php echo h($data['balance']) ?>" >
                    </div>
                </div> 
                <br>

                <div class="form-group">
                    <label>Follow Up 1 : - </label>
                    <textarea class="form-control" rows="2" name="followUp1" ><?php echo h($data['followUp1']) ?></textarea>
                </div>

                <div class="form-group">
                    <label>Follow Up 2 : - </label>
                    <textarea class="form-control" rows="2" name="followUp2" ><?php echo h($data['followUp2']) ?></textarea>
                </div>

                <div class="form-group">
                    <label>Follow Up 3 : - </label>
                    <textarea class="form-control" rows="2" name="followUp3" ><?php echo h($data['followUp3']) ?></textarea>
                </div>

                <div class="form-group">
                    <label>Follow Up 4 : - </label>
                    <textarea class="form-control" rows="2" name="followUp4" ><?php echo h($data['followUp4']) ?></textarea>
                </div>

                <button class="btn btn-primary" id="submitBtn" type="submit">Update Record</button>
                <br>
                <br>

            </form>

        </div>
    </div>
</div>


<?php
include './includes/footer.php';
?>