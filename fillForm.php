<?php
    include './includes/header.php';
?>
    
    <br>
    <div class="container">
        <h1> Patient Details<br>
        <small>Mahajan Homeo Clinic</small>
        </h1>
    </div>

    <form action="./php/insertRecord.php" method="get" class="container" onsubmit="setFormSubmitting()">
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>First name</label>
                <input type="text" id="firstName" class="form-control" name="firstName" placeholder="First name" required>
            </div>
            <div class="col-md-4 mb-3">
                <label>Middle name</label>
                <input type="text" id="middleName" class="form-control" name="middleName" placeholder="Middle name"  >
            </div>
            <div class="col-md-4 mb-3">
                <label>Last name</label>
                <input type="text" id="lastName" class="form-control" name="lastName" placeholder="Last name" required>
            </div>
        </div>
        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Age</label>
                <input type="text" id="age" class="form-control" name="age" placeholder="Age" required>
            </div>
            <div class="col-md-4 mb-3">
                <label>Sex</label>
                <!--<input type="text" id="sex" class="form-control" name="sex" placeholder="Gender"  required>-->
                <select class="form-control" id="sex" class="form-control" name="sex" required>
                    <option>None</option>
                    <option>Male</option>
                    <option>Female</option>
                    <option>Other</option>
                </select>
            </div>
            <div class="col-md-4 mb-3">
                <label>Occupation</label>
                <input type="text" id="occupation" class="form-control" name="occupation" placeholder="Occupation">
            </div>
        </div> 
        <br>

        <div class="form-group">
                <label>Address : </label>
                <input type="text" id="address" class="form-control input-lg" name="address" placeholder="Address" required>
        </div> <br>

        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Phone Number : </label>
                <input type="text" class="form-control" id="phone" name="phone" placeholder="Phone" maxlength="15">
            </div> 
            <div class="col-md-4 mb-3">
                <label>Registration Number : </label>
                <input type="text" class="form-control" id="regno" name="regno" placeholder="Registration Number">
            </div>
             
        </div>

        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label>Height : </label>
                <input type="text" class="form-control" id="height" name="height" placeholder="Height (in cm)">
            </div>
            <div class="col-md-4 mb-3">
                <label>Weight : </label>
                <input type="text" class="form-control" id="weight" name="weight" placeholder="Weight">
            </div> 
            <div class="col-md-4 mb-3">
                <label>Clinical Diagnosis: </label>
                <input type="text" class="form-control" id="diagnosis" name="diagnosis" placeholder="Diagnosis">
            </div>
        </div>

        <br>
        <h3>Chief Complaints</h3>
        <div class="form-group">
            <label>CC 1 : - </label>
            <input type="text" class="form-control" name="cc1" placeholder="Complaint 1">
        </div>
        
        <div class="form-group">
            <label>CC 2 : - </label>
            <input type="text" class="form-control" name="cc2" placeholder="Complaint 2">
        </div>
        
        <div class="form-group">
            <label>CC 3 : - </label>
            <input type="text" class="form-control" name="cc3" placeholder="Complaint 3">
        </div>

        <div class="form-group">
            <label>Appetite : - </label>
            <textarea class="form-control" rows="2" name="appetite"  placeholder="How's the Appetite of the patient?"></textarea>
        </div>

        <div class="form-group">
            <label>Desire : - </label>
            <textarea class="form-control" rows="5" name="desire" placeholder="What does the Patient Like?"></textarea>
        </div>

        <div class="form-group">
            <label>Aversions : - </label>
            <textarea class="form-control" rows="2" name="aversions"  placeholder="What are the Aversions?"></textarea>
        </div>
        
        <div class="form-group">
            <label>Thirst : - </label>
            <textarea class="form-control" rows="2" name="thirst" placeholder="How's the Thirst?"></textarea>
        </div>
        
        <div class="form-group">
            <label>Perspiration : - </label>
            <textarea class="form-control" rows="2" name="perspiration" placeholder="Perspiration ..."></textarea>
        </div>

        <div class="form-group">
            <label>Sleep : - </label>
            <textarea class="form-control" rows="2" name="sleep" placeholder="Do you sleep Well?"></textarea>
        </div>

        <div class="form-group">
            <label>Stool : - </label>
            <textarea class="form-control" rows="2" name="stool" placeholder="How is the Morning ritual?"></textarea>
        </div>

        <div class="form-group">
            <label>Urine : - </label>
            <textarea class="form-control" rows="2" name="urine" placeholder="Do you Drink lots of water?"></textarea>
        </div>

        <div class="form-group">
            <label>Menses : - </label>
            <textarea class="form-control" rows="2" name="menses" placeholder="Menstrual Cycle"></textarea>
        </div>
        

        <div class="form-group">
            <label>Thermal : - </label>
            <textarea class="form-control" rows="1" name="thermal" placeholder="Body Temperature conditions"></textarea>
        </div>

        <div class="form-group">
            <label>Mind : - </label>
            <textarea class="form-control" rows="6" name="mind" placeholder="Most Imp Ques? "></textarea>
        </div>

        <div class="form-group">
            <label>Hobbies : - </label>
            <textarea class="form-control" rows="2" name="hobbies" placeholder="What are your hobbies?"></textarea>
        </div>

        <div class="form-group">
            <label>Particulars : - </label>
            <textarea class="form-control" rows="8" name="particulars" placeholder="Particulars ..."></textarea>
        </div>


        <div class="form-group">
            <label>On-Examination : - </label>
            <textarea class="form-control" rows="4" name="on_examination" placeholder="What did you seeeee ...."></textarea>
        </div>

        <div class="form-group">
            <label>Path-Inv : - </label>
            <textarea class="form-control" rows="4" name="path_inv" placeholder="Pathelogy Investigation"></textarea>
        </div>

        <div class="form-group">
            <label>Previous-Rx : - </label>
            <textarea class="form-control" rows="4" name="previous_rx" placeholder="Have you taken some previous Statements"></textarea>
        </div>


        <div class="form-group">
            <label>Past-history : - </label>
            <textarea class="form-control" rows="4" name="past_history" placeholder="What's your history?"></textarea>
        </div>


        <div class="form-group">
            <label>Family-history : - </label>
            <textarea class="form-control" rows="2" name="family_history" placeholder="Family background history"></textarea>
        </div>

        <div class="form-group">
            <label>Treatment : - </label>
            <textarea class="form-control" rows="3" name="treatment" placeholder="Treatment Given"></textarea>
        </div>

        <div class="form-row">
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

        <div class="form-group">
            <label>Follow Up 1 : - </label>
            <textarea class="form-control" rows="2" name="followUp1" placeholder="Leave Blank if first time"></textarea>
        </div>

        <div class="form-group">
            <label>Follow Up 2 : - </label>
            <textarea class="form-control" rows="2" name="followUp2" placeholder="Leave Blank if first time"></textarea>
        </div>
        
        <div class="form-group">
            <label>Follow Up 3 : - </label>
            <textarea class="form-control" rows="2" name="followUp3" placeholder="Leave Blank if first time"></textarea>
        </div>
        
        <div class="form-group">
            <label>Follow Up 4 : - </label>
            <textarea class="form-control" rows="2" name="followUp4" placeholder="Leave Blank if first time"></textarea>
        </div>
        
        <button id="submitBtn" class="btn btn-primary" type="submit">Submit form</button>
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
    include './includes/footer.php';
?>
