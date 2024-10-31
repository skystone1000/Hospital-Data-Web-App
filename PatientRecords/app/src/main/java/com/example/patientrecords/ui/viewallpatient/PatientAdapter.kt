package com.example.patientrecords.ui.viewallpatient

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.databinding.ItemPatientBinding
import com.example.patientrecords.ui.followuppatient.PatientFollowUpActivity
import com.example.patientrecords.ui.patienthistory.PatientHistoryActivity

class PatientAdapter(private val patients: List<Patient>) :
    RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(val binding: ItemPatientBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        with(holder.binding) {
            tvName.text = "${patient.firstName} ${patient.lastName}"
            tvSex.text = patient.sex ?: "N/A"
            tvOccupation.text = patient.occupation ?: "N/A"
            tvPhone.text = patient.phone ?: "N/A"
            tvRegNo.text = patient.regno ?: "N/A"
        }

        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, PatientHistoryActivity::class.java).apply {
                putExtra("patient_id", patient.id)
                putExtra("patient_reg_no", patient.regno)
            }
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int = patients.size
}
