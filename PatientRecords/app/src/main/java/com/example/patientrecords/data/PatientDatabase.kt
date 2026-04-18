package com.example.patientrecords.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientDao
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.example.patientrecords.data.localdb.PatientFollowUpDao

@Database(entities = [Patient::class, PatientFollowUp::class], version = 3)
abstract class PatientDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun patientFollowUpDao(): PatientFollowUpDao

    companion object {
        @Volatile private var INSTANCE: PatientDatabase? = null

        // Removes followUp1-4 legacy columns from patient_data.
        // Recreates follow_up_data with FK to patient_data, cascade delete,
        // and follow_up_num changed from TEXT to INTEGER.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // --- patient_data: drop followUp1-4 columns ---
                database.execSQL("""
                    CREATE TABLE patient_data_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        firstName TEXT NOT NULL,
                        middleName TEXT,
                        lastName TEXT NOT NULL,
                        age INTEGER NOT NULL,
                        sex TEXT, occupation TEXT, address TEXT, phone TEXT, regno TEXT,
                        height INTEGER, weight INTEGER,
                        cc1 TEXT, cc2 TEXT, cc3 TEXT,
                        appetite TEXT, desire TEXT, aversions TEXT, thirst TEXT,
                        perspiration TEXT, sleep TEXT, stool TEXT, urine TEXT,
                        menses TEXT, thermal TEXT, mind TEXT, hobbies TEXT,
                        particulars TEXT, on_examination TEXT, path_inv TEXT,
                        previous_rx TEXT, past_history TEXT, family_history TEXT,
                        treatment TEXT, paid TEXT, balance TEXT,
                        dateJoined INTEGER, urlToImage TEXT
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO patient_data_new
                    SELECT id, firstName, middleName, lastName, age, sex, occupation, address,
                           phone, regno, height, weight, cc1, cc2, cc3, appetite, desire,
                           aversions, thirst, perspiration, sleep, stool, urine, menses, thermal,
                           mind, hobbies, particulars, on_examination, path_inv, previous_rx,
                           past_history, family_history, treatment, paid, balance,
                           dateJoined, urlToImage
                    FROM patient_data
                """.trimIndent())
                database.execSQL("DROP TABLE patient_data")
                database.execSQL("ALTER TABLE patient_data_new RENAME TO patient_data")

                // --- follow_up_data: add FK + change follow_up_num to INTEGER ---
                database.execSQL("""
                    CREATE TABLE follow_up_data_new (
                        followUpId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        id INTEGER NOT NULL DEFAULT -1,
                        date INTEGER NOT NULL DEFAULT 0,
                        regno TEXT NOT NULL DEFAULT '',
                        follow_up_num INTEGER NOT NULL DEFAULT 0,
                        weight INTEGER NOT NULL DEFAULT -1,
                        treatment_output TEXT NOT NULL DEFAULT '',
                        other_complains TEXT NOT NULL DEFAULT '',
                        treatment TEXT NOT NULL DEFAULT '',
                        medicine_duration TEXT NOT NULL DEFAULT '',
                        paid TEXT NOT NULL DEFAULT '',
                        balance INTEGER NOT NULL DEFAULT -1,
                        FOREIGN KEY(id) REFERENCES patient_data(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_follow_up_data_id ON follow_up_data_new (id)"
                )
                database.execSQL("""
                    INSERT INTO follow_up_data_new
                    SELECT followUpId, id, date, regno,
                           CAST(follow_up_num AS INTEGER),
                           weight, treatment_output, other_complains, treatment,
                           medicine_duration, paid, balance
                    FROM follow_up_data
                """.trimIndent())
                database.execSQL("DROP TABLE follow_up_data")
                database.execSQL("ALTER TABLE follow_up_data_new RENAME TO follow_up_data")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE patient_data ADD COLUMN diagnosis TEXT")
            }
        }

        fun getInstance(context: Context): PatientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatientDatabase::class.java,
                    "patient_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
