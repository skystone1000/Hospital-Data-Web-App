package com.example.patientrecords.ui.viewallpatient

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.databinding.ItemPatientBinding
import com.example.patientrecords.ui.patienthistory.PatientHistoryActivity
import com.example.patientrecords.utils.Extensions.Companion.EXTRA_PATIENT_ID

class PatientAdapter : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    inner class PatientViewHolder(val binding: ItemPatientBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = getItem(position) // Updated to use ListAdapter's getItem
        with(holder.binding) {
            tvName.text = "${patient.firstName} ${patient.middleName} ${patient.lastName}"
            tvSex.text = patient.sex ?: "N/A"
            tvOccupation.text = patient.occupation ?: "N/A"
            tvPhone.text = patient.phone ?: "N/A"
            tvRegNo.text = patient.regno ?: "N/A"
        }

        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, PatientHistoryActivity::class.java).apply {
                putExtra(EXTRA_PATIENT_ID, patient.id)
            }
            context.startActivity(intent)
        }
    }

    class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }

}
