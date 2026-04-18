package com.example.patientrecords.data.sqlimport

import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object SqlImportMapper {

    private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun toPatient(row: Map<String, String?>): Patient {
        fun str(col: String): String? = row[col]?.trim()?.takeIf { it.isNotEmpty() }
        fun int(col: String): Int? = row[col]?.trim()?.toIntOrNull()
        fun parseDate(col: String): Long? = row[col]?.let {
            runCatching { DATE_FMT.parse(it)?.time }.getOrNull()
        }

        val phone = str("phone")?.let { if (it == "2147483647" || it == "0") null else it }
        val height = int("height")?.takeIf { it != 0 }
        val weight = int("weight")?.takeIf { it != 0 }

        return Patient(
            id           = int("id") ?: throw IllegalArgumentException("missing id"),
            firstName    = row["firstName"]?.trim() ?: "",
            middleName   = str("middleName"),
            lastName     = row["lastName"]?.trim() ?: "",
            age          = int("age") ?: 0,
            sex          = str("sex"),
            occupation   = str("occupation"),
            address      = str("address"),
            phone        = phone,
            regno        = str("regno"),
            height       = height,
            weight       = weight,
            diagnosis    = str("diagnosis"),
            cc1          = str("cc1"),
            cc2          = str("cc2"),
            cc3          = str("cc3"),
            appetite     = str("appetite"),
            desire       = str("desire"),
            aversions    = str("aversions"),
            thirst       = str("thirst"),
            perspiration = str("perspiration"),
            sleep        = str("sleep"),
            stool        = str("stool"),
            urine        = str("urine"),
            menses       = str("menses"),
            thermal      = str("thermal"),
            mind         = str("mind"),
            hobbies      = str("hobbies"),
            particulars  = str("particulars"),
            on_examination = str("on_examination"),
            path_inv     = str("path_inv"),
            previous_rx  = str("previous_rx"),
            past_history = str("past_history"),
            family_history = str("family_history"),
            treatment    = str("treatment"),
            paid         = str("paid"),
            balance      = str("balance"),
            dateJoined   = parseDate("dateJoined"),
            urlToImage   = null
        )
    }

    fun toFollowUp(row: Map<String, String?>): PatientFollowUp {
        fun str(col: String): String = row[col]?.trim() ?: ""

        val weightStr = str("weight")
        val weight = Regex("""\d+(\.\d+)?""").find(weightStr)?.value?.toFloatOrNull()?.toInt() ?: -1
        val balance = str("balance").trim().toIntOrNull() ?: -1
        val date = row["date"]?.let {
            runCatching { DATE_FMT.parse(it)?.time }.getOrNull()
        } ?: System.currentTimeMillis()

        return PatientFollowUp(
            followUpId        = row["followUpId"]?.trim()?.toIntOrNull() ?: 0,
            id                = row["id"]?.trim()?.toIntOrNull() ?: -1,
            date              = date,
            regno             = str("regno"),
            follow_up_num     = str("follow_up_num").toIntOrNull() ?: 0,
            weight            = weight,
            treatment_output  = str("treatment_output"),
            other_complains   = str("other_complains"),
            treatment         = str("treatment"),
            medicine_duration = str("medicine_duration"),
            paid              = str("paid"),
            balance           = balance
        )
    }
}
