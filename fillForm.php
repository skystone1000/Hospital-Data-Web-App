<?php
	include './includes/header.php';
?>

<div class="row">
	<div class="col-2" >
        <div class="sticky-top" style="overflow-y: scroll; max-height: 650px ; ">
        <nav id="navbar-example3" class="navbar navbar-light bg-light">
            <a class="navbar-brand" href="#">Attributes</a>
            <nav class="nav nav-pills flex-column">
                <!--
                <a class="nav-link" href="#item-1">Item 1</a>
                <nav class="nav nav-pills flex-column">
                    <a class="nav-link ml-3 my-1" href="#item-1-1">Item 1-1</a>
                    <a class="nav-link ml-3 my-1" href="#item-1-2">Item 1-2</a>
                </nav>
                <a class="nav-link" href="#item-2">Item 2</a>
                <a class="nav-link" href="#item-3">Item 3</a>
                <nav class="nav nav-pills flex-column">
                    <a class="nav-link ml-3 my-1" href="#item-3-1">Item 3-1</a>
                    <a class="nav-link ml-3 my-1" href="#item-3-2">Item 3-2</a>
                </nav>
                -->
                <a class="nav-link" href="#cc0">Chief Complains</a>
                <a class="nav-link" href="#appetite">Appetite</a>
                <a class="nav-link" href="#desire">Desire</a>
                <a class="nav-link" href="#aversions">Aversions</a>
                <a class="nav-link" href="#thirst">Thirst</a>
                <a class="nav-link" href="#perspiration">Perspiration</a>
                <a class="nav-link" href="#sleep">Sleep</a>
                <a class="nav-link" href="#stool">Stool</a>
                <a class="nav-link" href="#urine">Urine</a>
                <a class="nav-link" href="#menses">Menses</a>
                <a class="nav-link" href="#thermal">Thermal</a>
                <a class="nav-link" href="#mind">Mind</a>
                <a class="nav-link" href="#hobbies">Hobbies</a>
                <a class="nav-link" href="#particulars">Particulars</a>
                <a class="nav-link" href="#on_examination">On Examination</a>
                <a class="nav-link" href="#path_inv">Path Inv</a>
                <a class="nav-link" href="#previous_rx">Previous Rx</a>
                <a class="nav-link" href="#past_history">Past History</a>
                <a class="nav-link" href="#family_history">Family History</a>
                <a class="nav-link" href="#treatment">Treatment</a>
                <a class="nav-link" href="#payment">Payment</a>
                <a class="nav-link" href="#submitBtn">Submit</a>
            </nav>
        </nav>
        </div>
	</div>
	<div class="col-10">


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
        <h3 id="cc0">Chief Complaints</h3>
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
            <label id="appetite">Appetite : - </label>
            <textarea class="form-control" rows="2" name="appetite"  placeholder="How's the Appetite of the patient?"></textarea>
        </div>

        <div class="form-group">
            <label id="desire">Desire : - </label>
            <textarea class="form-control" rows="5" name="desire" placeholder="What does the Patient Like?"></textarea>
        </div>

        <div class="form-group">
            <label id="aversions">Aversions : - </label>
            <textarea class="form-control" rows="2" name="aversions"  placeholder="What are the Aversions?"></textarea>
        </div>
        
        <div class="form-group">
            <label id="thirst">Thirst : - </label>
            <textarea class="form-control" rows="2" name="thirst" placeholder="How's the Thirst?"></textarea>
        </div>
        
        <div class="form-group">
            <label id="perspiration">Perspiration : - </label>
            <textarea class="form-control" rows="2" name="perspiration" placeholder="Perspiration ..."></textarea>
        </div>

        <div class="form-group">
            <label id="sleep">Sleep : - </label>
            <textarea class="form-control" rows="2" name="sleep" placeholder="Do you sleep Well?"></textarea>
        </div>

        <div class="form-group">
            <label id="stool">Stool : - </label>
            <textarea class="form-control" rows="2" name="stool" placeholder="How is the Morning ritual?"></textarea>
        </div>

        <div class="form-group">
            <label id="urine">Urine : - </label>
            <textarea class="form-control" rows="2" name="urine" placeholder="Do you Drink lots of water?"></textarea>
        </div>

        <div class="form-group">
            <label id="menses">Menses : - </label>
            <textarea class="form-control" rows="2" name="menses" placeholder="Menstrual Cycle"></textarea>
        </div>
        

        <div class="form-group">
            <label id="thermal">Thermal : - </label>
            <textarea class="form-control" rows="1" name="thermal" placeholder="Body Temperature conditions"></textarea>
        </div>

        <div class="form-group">
            <label id="mind">Mind : - </label>
            <textarea class="form-control" rows="6" name="mind" placeholder="Most Imp Ques? "></textarea>
        </div>

        <div class="form-group">
            <label id="hobbies">Hobbies : - </label>
            <textarea class="form-control" rows="2" name="hobbies" placeholder="What are your hobbies?"></textarea>
        </div>

        <div class="form-group">
            <label id="particulars">Particulars : - </label>
            <textarea class="form-control" rows="8" name="particulars" placeholder="Particulars ..."></textarea>
        </div>


        <div class="form-group">
            <label id="on_examination">On-Examination : - </label>
            <textarea class="form-control" rows="4" name="on_examination" placeholder="What did you seeeee ...."></textarea>
        </div>

        <div class="form-group">
            <label id="path_inv">Path-Inv : - </label>
            <textarea class="form-control" rows="4" name="path_inv" placeholder="Pathelogy Investigation"></textarea>
        </div>

        <div class="form-group">
            <label id="previous_rx">Previous-Rx : - </label>
            <textarea class="form-control" rows="4" name="previous_rx" placeholder="Have you taken some previous Statements"></textarea>
        </div>


        <div class="form-group">
            <label id="past_history">Past-history : - </label>
            <textarea class="form-control" rows="4" name="past_history" placeholder="What's your history?"></textarea>
        </div>


        <div class="form-group">
            <label id="family_history">Family-history : - </label>
            <textarea class="form-control" rows="2" name="family_history" placeholder="Family background history"></textarea>
        </div>

        <div class="form-group">
            <label id="treatment">Treatment : - </label>
            <textarea class="form-control" rows="3" name="treatment" placeholder="Treatment Given"></textarea>
        </div>

        <div class="form-row">
            <div class="col-md-4 mb-3">
                <label id="payment">Paid Amount</label>
                <input type="text" id="paid" class="form-control" name="paid" placeholder="Paid" required>
            </div>
            <div class="col-md-4 mb-3">
                <label>Balance Amount</label>
                <input type="text" id="balance" class="form-control" name="balance" placeholder="Balance"  required>
            </div>
        </div> 
        <br>

        <!--
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
        -->
        
        <button id="submitBtn" class="btn btn-primary" type="submit">Submit form</button>
    </form>
    
    <br><br><br>





	</div>
</div>


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