<form action="#" method="get" class="container" onsubmit="setFormSubmitting()">

    <h2>Follow Up Details : </h2>
    <div class="form-row">
        <div class="col-md-4 mb-3">
            <label>Weight : </label>
            <input type="text" class="form-control" id="weight" name="weight" placeholder="<?php echo $dataRowF['weight']; ?>" readonly>
        </div>
        <div class="col-md-4 mb-3">
        	<label>Previous treatment Output : </label>
        	<select class="form-control" name="treatment_output" readonly>
			    <option>
                    <?php 
                        if(isset($id)){
                            echo $dataRowF['treatment_output'];
                        } else {
                    ?>
                    None
                </option>
			    <option>Better</option>
			    <option>Same</option>
			    <option>Worse</option>
                    <?php      }       ?>
			</select>
        </div>
        <div class="col-md-4 mb-3">
            <label>Date : </label>
            <input type="text" class="form-control" id="date" name="date" placeholder="<?php echo $dataRowF['date']; ?>" readonly>
        </div>
    </div>

    <div class="form-group">
        <label>Other Complains : - </label>
        <textarea class="form-control" rows="3" name="other_complains" placeholder="Other Complains" readonly><?php echo $dataRowF['other_complains']; ?></textarea>
    </div>

    <div class="form-group">
        <label>Treatment : - </label>
        <textarea class="form-control" rows="3" name="treatment" placeholder="Treatment Given" readonly><?php echo $dataRowF['treatment']; ?></textarea>
    </div>

    <div class="form-row">
        <div class="col-md-4 mb-3">
        	<label>Medicine Duration : </label>
        	<select class="form-control" name="medicine_duration" readonly>
			    <option>
                    <?php 
                        if(isset($id)){
                            echo $dataRowF['medicine_duration'];
                        } else {
                    ?>
                None
                </option>
			    <option>3 Days</option>
			    <option>8 Days</option>
			    <option>15 Days</option>
			    <option>1 Month</option>
                    <?php      }       ?>
			</select>
        </div>
        <div class="col-md-4 mb-3">
            <label>Paid Amount</label>
            <input type="text" id="paid" class="form-control" name="paid" placeholder="<?php echo $dataRowF['paid']; ?>" readonly>
        </div>
        <div class="col-md-4 mb-3">
            <label>Balance Amount</label>
            <input type="text" id="balance" class="form-control" name="balance" placeholder="<?php echo $dataRowF['balance']; ?>"  readonly>
        </div>
    </div> 
    <br>

</form>