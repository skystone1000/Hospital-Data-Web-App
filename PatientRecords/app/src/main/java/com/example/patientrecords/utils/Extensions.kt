package com.example.patientrecords.utils

import java.util.Calendar

open class Extensions {
    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_REG_NO = "patient_reg_no"
        const val EXTRA_FOLLOW_UP_NUMBER = "follow_up_number"
        const val EXTRA_VIEW_MODE = "is_view_mode"

        fun get1DayAgo(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return calendar.timeInMillis
        }

        fun get7DaysAgo(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            return calendar.timeInMillis
        }

        fun get31DaysAgo(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -31)
            return calendar.timeInMillis
        }

        fun get365DaysAgo(): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -365)
            return calendar.timeInMillis
        }

    }
}