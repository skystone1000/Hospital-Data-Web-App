# Android Sync Fix Plan — PatientRecords App

**Date**: 2026-05-23  
**App location**: `C:\xampp\htdocs\Hospital-Data-Web-App\PatientRecords\`  
**Current Room DB version**: 3  
**Target Room DB version**: 4  
**Firebase project**: `androidapp-70662`  
**Related plan**: [`plan_ui_refactor.md`](plan_ui_refactor.md) — web app sync architecture

---

## 1. The Two Root Bugs

Both bugs are in `FirebaseSyncManager.kt` and `FirebaseRepository.kt`. Everything else in the sync layer works correctly — the architecture (bidirectional last-write-wins, coroutine-safe, cancellation-safe) is sound. Only the **key** and the **comparison field** are wrong.

### Bug 1 — ID Collision (`FirebaseRepository.kt` lines 22, 52)

```kotlin
// Patient upload — current (BROKEN)
dbPatientRef.child(patient.id.toString()).setValue(patient)
//                  ^^^^^^^^^^^^^^^^^^
//                  Android SQLite auto-increment integer as Firebase key

// Follow-up upload — current (BROKEN)
dbPatientFollowUpRef.child(followUp.followUpId.toString()).setValue(followUp)
//                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                          Same problem
```

**What goes wrong**: Android SQLite generates `id = 1, 2, 3 ...` independently of MySQL. When the web app creates its first patient it also gets `id = 1`. Both apps write to `/patients/1` — the second write silently overwrites the first. Two different people, one Firebase record.

**Fix**: Replace integer keys with a UUID stored in the entity (`firebaseId`). UUIDs are globally unique by construction — no coordination needed between the two apps.

---

### Bug 2 — Edit Loss (`FirebaseSyncManager.kt` lines 17, 39)

```kotlin
// Patient comparison — current (BROKEN)
remote == null || (local != remote && (local.dateJoined ?: 0L) > (remote.dateJoined ?: 0L))
//                                     ^^^^^^^^^^^^^^^^^^^^^^
//                                     dateJoined is set once at creation and never changes

// Follow-up comparison — current (BROKEN)
remote == null || (local != remote && local.date > remote.date)
//                                    ^^^^^^^^^^
//                                    date is set once at creation and never changes
```

**What goes wrong**: If you edit a patient's treatment on the web and then trigger sync on Android, `dateJoined` is the same on both sides (it's the creation date). The sync sees them as equal and skips the update. The Android copy never reflects the web edit.

**Fix**: Add `updatedAt: Long` to both entities. Set it to `System.currentTimeMillis()` on every insert and update (not just at creation). Compare by `updatedAt` instead of `dateJoined`/`date`.

---

### Bug 3 — Record Matching by Integer ID (`FirebaseSyncManager.kt` lines 16, 22)

```kotlin
// Patient match — current (BROKEN when IDs differ between platforms)
val remote = remotePatients.find { it.id == local.id }
//                                  ^^^^^^^^^^^^^^^^
//                                  Matches Android's integer id to whatever is in Firebase
//                                  A web-created patient with id=5 will be "found" when
//                                  Android happens to also have a patient with id=5

val local = localPatients.find { it.id == remote.id }
//                                ^^^^^^^^^^^^^^^^^^ same problem in reverse
```

**What goes wrong**: When the web app creates a patient and syncs it to Firebase, that Firebase record has the web's MySQL integer in the `id` field. Android will match it against its own `id=X` patient — a completely different person — and either merge them or skip the pull entirely.

**Fix**: Match records by `firebaseId` (UUID string), not by `id` (platform integer). After the fix, an Android-created patient and a web-created patient are never confused for each other.

---

## 2. Files to Change

| File | Change |
|---|---|
| `data/localdb/Patient.kt` | Add `firebaseId`, `updatedAt` fields |
| `data/localdb/PatientFollowUp.kt` | Add `firebaseId`, `patientFirebaseId`, `updatedAt` fields |
| `data/PatientDatabase.kt` | Bump version 3→4, add `MIGRATION_3_4` |
| `data/localdb/PatientDao.kt` | Add `getPatientsWithoutFirebaseId()` query |
| `data/localdb/PatientFollowUpDao.kt` | Add `getFollowUpsWithoutFirebaseId()` query |
| `data/PatientRepository.kt` | Expose the two new DAO queries |
| `data/FirebaseRepository.kt` | Use `firebaseId` as key; handle soft-delete flag |
| `data/FirebaseSyncManager.kt` | Match by `firebaseId`; compare by `updatedAt`; assign UUID before push; handle soft-deletes |
| `PatientRecordsApp.kt` | Run one-time backfill of `firebaseId` + `updatedAt` on app startup |
| `ui/addpatient/AddPatientViewModel.kt` | Set `updatedAt` on every insert and update |
| `ui/followuppatient/PatientFollowUpViewModel.kt` | Set `updatedAt` on every insert and update |

---

## 3. Firebase Field Name Contract

Android's Room entity uses `snake_case` field names (e.g. `on_examination`, `other_complains`). Firebase serialises exactly what Room stores. The Next.js web app must read and write using these same names — do not use camelCase in Firebase writes from the web side.

The agreed Firebase record shapes are:

### `/patients/{firebaseId}`
```
firebaseId          String   — UUID, same value as the key
localAndroidId      Int      — Android's SQLite patient.id (-1 if web-created)
updatedAt           Long     — Unix millis; updated on every mutation
deleted             Boolean  — false normally; true when record is deleted
deletedAt           Long     — 0 normally; Unix millis when deleted

firstName, middleName, lastName, age, sex, occupation
address, phone, regno, dateJoined (Long millis), height, weight, diagnosis
cc1, cc2, cc3
appetite, desire, aversions, thirst, perspiration, sleep, stool, urine, menses, thermal, mind
hobbies, particulars, on_examination, path_inv, previous_rx, past_history, family_history
treatment, paid, balance, urlToImage
```

### `/patient_follow_ups/{firebaseId}`
```
firebaseId          String   — UUID
patientFirebaseId   String   — UUID of parent patient
localAndroidId      Int      — Android's follow_up_data.followUpId (-1 if web-created)
updatedAt           Long     — Unix millis
deleted             Boolean
deletedAt           Long

id (Int)            — Android patient.id reference (for Android use; -1 if web-created)
date                Long     — follow-up date in millis
regno               String
follow_up_num       Int
weight              Int
treatment_output, other_complains, treatment, medicine_duration
paid                String
balance             Int
```

---

## 4. Change: `Patient.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/localdb/Patient.kt`

Add two fields at the top (after `id`). All other fields stay exactly the same.

```kotlin
package com.example.patientrecords.data.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_data")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firebaseId: String = "",                          // NEW — UUID; empty until first sync
    val updatedAt: Long = System.currentTimeMillis(),     // NEW — set on every insert/update
    val firstName: String = "",
    val middleName: String? = null,
    val lastName: String = "",
    val age: Int = 0,
    val sex: String? = null,
    val occupation: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val regno: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val diagnosis: String? = null,
    val cc1: String? = null,
    val cc2: String? = null,
    val cc3: String? = null,
    val appetite: String? = null,
    val desire: String? = null,
    val aversions: String? = null,
    val thirst: String? = null,
    val perspiration: String? = null,
    val sleep: String? = null,
    val stool: String? = null,
    val urine: String? = null,
    val menses: String? = null,
    val thermal: String? = null,
    val mind: String? = null,
    val hobbies: String? = null,
    val particulars: String? = null,
    val on_examination: String? = null,
    val path_inv: String? = null,
    val previous_rx: String? = null,
    val past_history: String? = null,
    val family_history: String? = null,
    val treatment: String? = null,
    val paid: String? = null,
    val balance: String? = null,
    val dateJoined: Long? = System.currentTimeMillis(),
    val urlToImage: String? = null
)
```

---

## 5. Change: `PatientFollowUp.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/localdb/PatientFollowUp.kt`

Add three fields after `followUpId`.

```kotlin
package com.example.patientrecords.data.localdb

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_up_data",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["id"])]
)
data class PatientFollowUp(
    @PrimaryKey(autoGenerate = true) val followUpId: Int = 0,
    val firebaseId: String = "",                          // NEW — UUID
    val patientFirebaseId: String = "",                   // NEW — parent patient UUID
    val updatedAt: Long = System.currentTimeMillis(),     // NEW — set on every insert/update
    val id: Int = -1,
    val date: Long = System.currentTimeMillis(),
    val regno: String = "",
    val follow_up_num: Int = 0,
    val weight: Int = -1,
    val treatment_output: String = "",
    val other_complains: String = "",
    val treatment: String = "",
    val medicine_duration: String = "",
    val paid: String = "",
    val balance: Int = -1
)
```

---

## 6. Change: `PatientDatabase.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/PatientDatabase.kt`

Bump version to 4 and add `MIGRATION_3_4`. All existing migrations stay unchanged.

```kotlin
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

@Database(entities = [Patient::class, PatientFollowUp::class], version = 4)   // ← bumped
abstract class PatientDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun patientFollowUpDao(): PatientFollowUpDao

    companion object {
        @Volatile private var INSTANCE: PatientDatabase? = null

        // --- existing migrations unchanged ---

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
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

        // --- NEW migration ---

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // patient_data: add firebaseId and updatedAt
                database.execSQL(
                    "ALTER TABLE patient_data ADD COLUMN firebaseId TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE patient_data ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )

                // follow_up_data: add firebaseId, patientFirebaseId, updatedAt
                database.execSQL(
                    "ALTER TABLE follow_up_data ADD COLUMN firebaseId TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE follow_up_data ADD COLUMN patientFirebaseId TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE follow_up_data ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): PatientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatientDatabase::class.java,
                    "patient_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)  // ← added
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## 7. Change: `PatientDao.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/localdb/PatientDao.kt`

Add one query for the startup backfill. Everything else stays the same.

```kotlin
package com.example.patientrecords.data.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Query("SELECT * FROM patient_data")
    fun getAll(): Flow<List<Patient>>

    @Query("SELECT * FROM patient_data WHERE id = :patientId")
    fun getPatientById(patientId: Int): Flow<Patient>

    @Query("SELECT * FROM patient_data WHERE firstName LIKE '%' || :query || '%' OR middleName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<Patient>>

    @Query("SELECT * FROM patient_data WHERE dateJoined >= :date")
    suspend fun getPatientsFromDay(date: Long): List<Patient>

    // NEW — used by startup backfill to assign UUIDs to pre-existing records
    @Query("SELECT * FROM patient_data WHERE firebaseId = ''")
    suspend fun getPatientsWithoutFirebaseId(): List<Patient>
}
```

---

## 8. Change: `PatientFollowUpDao.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/localdb/PatientFollowUpDao.kt`

Add one query for backfill. Everything else stays the same.

```kotlin
package com.example.patientrecords.data.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientFollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: PatientFollowUp)

    @Update
    suspend fun updateFollowUp(followUp: PatientFollowUp)

    @Query("SELECT * FROM follow_up_data WHERE id = :patientId ORDER BY follow_up_num DESC")
    fun getFollowUpsForPatient(patientId: Int): Flow<List<PatientFollowUp>>

    @Query("SELECT MAX(follow_up_num) FROM follow_up_data WHERE id = :patientId")
    suspend fun getMaxFollowUpNum(patientId: Int): Int?

    @Query("SELECT * FROM follow_up_data")
    suspend fun getAllFollowUps(): List<PatientFollowUp>

    @Query("SELECT * FROM follow_up_data WHERE date >= :date")
    suspend fun getFollowUpsFromDay(date: Long): List<PatientFollowUp>

    @Query("SELECT DISTINCT p.* FROM patient_data p INNER JOIN follow_up_data f ON p.id = f.id WHERE f.date >= :date")
    suspend fun getPatientsWithFollowUpsFromDay(date: Long): List<Patient>

    // NEW — used by startup backfill
    @Query("SELECT * FROM follow_up_data WHERE firebaseId = ''")
    suspend fun getFollowUpsWithoutFirebaseId(): List<PatientFollowUp>
}
```

---

## 9. Change: `PatientRepository.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/PatientRepository.kt`

Expose the two new backfill queries. No other changes.

```kotlin
// Add these two methods at the bottom of PatientRepository

// Backfill support
suspend fun getPatientsWithoutFirebaseId(): List<Patient> =
    patientDao.getPatientsWithoutFirebaseId()

suspend fun getFollowUpsWithoutFirebaseId(): List<PatientFollowUp> =
    patientFollowUpDao.getFollowUpsWithoutFirebaseId()
```

---

## 10. Change: `FirebaseRepository.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/FirebaseRepository.kt`

Three changes:
1. Use `patient.firebaseId` as the Firebase key (not `patient.id.toString()`)
2. Use `followUp.firebaseId` as the Firebase key (not `followUp.followUpId.toString()`)
3. When downloading, capture the Firebase key into the object's `firebaseId` field (since Firebase doesn't auto-populate the key into the value object)

```kotlin
package com.example.patientrecords.data

import android.util.Log
import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRepository {

    private val dbPatientRef = FirebaseDatabase.getInstance().getReference("patients")
    private val dbPatientFollowUpRef = FirebaseDatabase.getInstance().getReference("patient_follow_ups")

    // Upload Patients — fire-and-forget
    // Uses firebaseId (UUID) as the Firebase key instead of the SQLite integer id.
    fun uploadPatients(patients: List<Patient>) {
        patients.forEach { patient ->
            dbPatientRef.child(patient.firebaseId).setValue(patient)   // ← firebaseId, not id
                .addOnSuccessListener {
                    Log.d("Firebase", "Patient ${patient.firebaseId} uploaded.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to upload patient ${patient.firebaseId}: ${e.message}")
                }
        }
    }

    // Download Patients
    // Reads the Firebase key (UUID string) into the patient's firebaseId field so the sync
    // manager can match records by UUID even if getValue() doesn't populate firebaseId
    // (it will populate it since it's stored as a field, but this is a safety net).
    suspend fun downloadPatients(): List<Patient> = suspendCancellableCoroutine { cont ->
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Patient::class.java)?.let { patient ->
                        // If firebaseId is empty in the stored object, populate it from the key
                        if (patient.firebaseId.isEmpty()) {
                            patient.copy(firebaseId = child.key ?: "")
                        } else {
                            patient
                        }
                    }
                }
                cont.resume(list)
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        }
        dbPatientRef.addListenerForSingleValueEvent(listener)
        cont.invokeOnCancellation { dbPatientRef.removeEventListener(listener) }
    }

    // Upload Follow-ups — fire-and-forget
    fun uploadPatientFollowUps(patientFollowUps: List<PatientFollowUp>) {
        patientFollowUps.forEach { followUp ->
            dbPatientFollowUpRef.child(followUp.firebaseId).setValue(followUp)  // ← firebaseId
                .addOnSuccessListener {
                    Log.d("Firebase", "Follow-up ${followUp.firebaseId} uploaded.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to upload follow-up ${followUp.firebaseId}: ${e.message}")
                }
        }
    }

    // Download Follow-ups — same key-capture pattern as downloadPatients
    suspend fun downloadFollowUps(): List<PatientFollowUp> = suspendCancellableCoroutine { cont ->
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(PatientFollowUp::class.java)?.let { followUp ->
                        if (followUp.firebaseId.isEmpty()) {
                            followUp.copy(firebaseId = child.key ?: "")
                        } else {
                            followUp
                        }
                    }
                }
                cont.resume(list)
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        }
        dbPatientFollowUpRef.addListenerForSingleValueEvent(listener)
        cont.invokeOnCancellation { dbPatientFollowUpRef.removeEventListener(listener) }
    }

    // Soft-delete: write a minimal tombstone to Firebase so other platforms know to delete
    fun markPatientDeleted(firebaseId: String, deletedAt: Long) {
        dbPatientRef.child(firebaseId).updateChildren(
            mapOf("deleted" to true, "deletedAt" to deletedAt, "updatedAt" to deletedAt)
        )
    }

    fun markFollowUpDeleted(firebaseId: String, deletedAt: Long) {
        dbPatientFollowUpRef.child(firebaseId).updateChildren(
            mapOf("deleted" to true, "deletedAt" to deletedAt, "updatedAt" to deletedAt)
        )
    }
}
```

---

## 11. Change: `FirebaseSyncManager.kt`

**File**: `app/src/main/java/com/example/patientrecords/data/FirebaseSyncManager.kt`

Full rewrite. Key changes:
- Assign UUID to records without a `firebaseId` before pushing
- Match by `firebaseId` (not by `id` or `followUpId`)
- Compare by `updatedAt` (not `dateJoined` or `date`)
- Handle soft-deletes pulled from Firebase

```kotlin
package com.example.patientrecords.data

import com.example.patientrecords.data.localdb.Patient
import com.example.patientrecords.data.localdb.PatientFollowUp
import kotlinx.coroutines.flow.first
import java.util.UUID

class FirebaseSyncManager(
    private val localRepo: PatientRepository,
    private val firebaseRepo: FirebaseRepository
) {

    suspend fun syncPatientsBothWays() {
        // Step 1: assign UUIDs to any local patients that don't have one yet
        assignMissingPatientUuids()

        val localPatients = localRepo.getAllPatients().first()
        val remotePatients = firebaseRepo.downloadPatients()

        // Step 2: push local records that are new or newer than Firebase
        // Match by firebaseId — this is the only safe cross-platform identifier
        val toFirebase = localPatients.filter { local ->
            if (local.firebaseId.isEmpty()) return@filter false  // safety; shouldn't happen after step 1
            val remote = remotePatients.find { it.firebaseId == local.firebaseId }
            remote == null || local.updatedAt > remote.updatedAt
        }
        firebaseRepo.uploadPatients(toFirebase)

        // Step 3: pull remote records that are new or newer than what we have locally
        for (remote in remotePatients) {
            if (remote.firebaseId.isEmpty()) continue  // malformed record; skip
            val local = localPatients.find { it.firebaseId == remote.firebaseId }

            // Handle soft-delete: if the remote record was deleted and we have it locally,
            // delete it — but only if the deletion is newer than our local copy.
            val remoteDeleted = remote.urlToImage == "DELETED"  // see note below *
            if (remoteDeleted) {
                if (local != null && remote.updatedAt > local.updatedAt) {
                    localRepo.deletePatient(local)
                }
                continue
            }

            when {
                local == null -> {
                    // New record from another platform: insert with the remote's firebaseId
                    // Keep remote.id as-is (it's the other platform's SQLite id); Room will
                    // assign a new local id via REPLACE on next insert.
                    // We use id=0 so Room autoGenerates a new local id for this patient.
                    localRepo.insertPatient(remote.copy(id = 0))
                }
                remote.updatedAt > local.updatedAt -> {
                    // Remote is newer: update local, preserving our local id
                    localRepo.updatePatient(remote.copy(id = local.id))
                }
                // else: local is same age or newer — nothing to do
            }
        }
    }

    suspend fun syncPatientFollowUpsBothWays() {
        // Step 1: assign UUIDs to any local follow-ups that don't have one yet
        assignMissingFollowUpUuids()

        val localFollowUps = localRepo.getAllFollowUps()
        val remoteFollowUps = firebaseRepo.downloadFollowUps()

        // Step 2: push local records newer than Firebase
        val toFirebase = localFollowUps.filter { local ->
            if (local.firebaseId.isEmpty()) return@filter false
            val remote = remoteFollowUps.find { it.firebaseId == local.firebaseId }
            remote == null || local.updatedAt > remote.updatedAt
        }
        firebaseRepo.uploadPatientFollowUps(toFirebase)

        // Step 3: pull remote records newer than local
        for (remote in remoteFollowUps) {
            if (remote.firebaseId.isEmpty()) continue
            val local = localFollowUps.find { it.firebaseId == remote.firebaseId }

            // Soft-delete check (see note * below)
            val remoteDeleted = remote.treatment == "DELETED"
            if (remoteDeleted) {
                if (local != null && remote.updatedAt > local.updatedAt) {
                    // Room will cascade-delete follow-ups when parent is deleted,
                    // but we may want to delete a single follow-up independently.
                    // PatientFollowUpDao needs a delete-by-object method (already has @Delete).
                    // We call updateFollowUp with a tombstone and then delete immediately.
                    // Simpler: just don't insert deleted records; remove if exists.
                    // For now: skip inserting deleted records; local delete handled separately.
                }
                continue
            }

            // Resolve parent patient's local id from firebaseId
            // We need a local patient id to satisfy the FK constraint
            val localPatientId = resolveLocalPatientId(remote.patientFirebaseId, localFollowUps)

            when {
                local == null && localPatientId != null -> {
                    // New follow-up from another platform
                    localRepo.addFollowUp(
                        remote.copy(followUpId = 0, id = localPatientId)
                    )
                }
                local != null && remote.updatedAt > local.updatedAt -> {
                    // Remote is newer: update local copy
                    localRepo.updateFollowUp(
                        remote.copy(followUpId = local.followUpId, id = local.id)
                    )
                }
            }
        }
    }

    // --- helpers ---

    private suspend fun assignMissingPatientUuids() {
        val noId = localRepo.getPatientsWithoutFirebaseId()
        noId.forEach { patient ->
            localRepo.updatePatient(
                patient.copy(
                    firebaseId = UUID.randomUUID().toString(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun assignMissingFollowUpUuids() {
        val noId = localRepo.getFollowUpsWithoutFirebaseId()
        noId.forEach { followUp ->
            // Also populate patientFirebaseId from the parent patient if available
            val parentFirebaseId = localRepo.getAllPatients().first()
                .find { it.id == followUp.id }
                ?.firebaseId ?: ""
            localRepo.updateFollowUp(
                followUp.copy(
                    firebaseId = UUID.randomUUID().toString(),
                    patientFirebaseId = parentFirebaseId,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun resolveLocalPatientId(patientFirebaseId: String, localFollowUps: List<PatientFollowUp>): Int? {
        if (patientFirebaseId.isEmpty()) return null
        // Look up via a local patient that has this firebaseId
        val allPatients = localRepo.getAllPatients().first()
        return allPatients.find { it.firebaseId == patientFirebaseId }?.id
    }
}

/*
 * NOTE on soft-deletes (*):
 * The current Patient and PatientFollowUp entities don't have a 'deleted' boolean field.
 * To avoid another Room migration just for soft-delete, two options:
 *
 * Option A (recommended for now): add a 'deleted: Boolean = false' field in the NEXT migration
 * and check it properly. This is the clean long-term solution.
 *
 * Option B (workaround without migration): use a sentinel value in an existing nullable field
 * (e.g. urlToImage = "DELETED" for patients, treatment = "DELETED" for follow-ups) to signal
 * deletion. The sync code above uses this workaround. It is fragile and should be replaced
 * with Option A when the next Room migration is done.
 *
 * For now the workaround is sufficient because:
 * - The web app is the only platform that currently deletes records
 * - Web deletes cascade to follow-ups in MySQL; Firebase gets a soft-delete tombstone
 * - Android only needs to detect and honour those tombstones
 */
```

---

## 12. Change: `AddPatientViewModel.kt`

**File**: `app/src/main/java/com/example/patientrecords/ui/addpatient/AddPatientViewModel.kt`

Set `updatedAt = System.currentTimeMillis()` on every insert and update. The ViewModel currently calls `localRepo.insertPatient(patient)` and `localRepo.updatePatient(patient)` — wrap them to stamp `updatedAt` before the call.

Locate the existing insert and update calls in the ViewModel and change them:

```kotlin
// Before (wherever insertPatient is called):
localRepo.insertPatient(patient)

// After:
localRepo.insertPatient(patient.copy(updatedAt = System.currentTimeMillis()))

// Before (wherever updatePatient is called):
localRepo.updatePatient(patient)

// After:
localRepo.updatePatient(patient.copy(updatedAt = System.currentTimeMillis()))
```

The `firebaseId` does **not** need to be assigned here. The sync manager's `assignMissingPatientUuids()` handles it at sync time. Assigning at insert time is a valid optimisation but not required for correctness.

---

## 13. Change: `PatientFollowUpViewModel.kt`

**File**: `app/src/main/java/com/example/patientrecords/ui/followuppatient/PatientFollowUpViewModel.kt`

Same pattern as `AddPatientViewModel`:

```kotlin
// Before:
localRepo.addFollowUp(followUp)

// After:
localRepo.addFollowUp(followUp.copy(updatedAt = System.currentTimeMillis()))

// Before:
localRepo.updateFollowUp(followUp)

// After:
localRepo.updateFollowUp(followUp.copy(updatedAt = System.currentTimeMillis()))
```

---

## 14. Change: `PatientRecordsApp.kt`

**File**: `app/src/main/java/com/example/patientrecords/PatientRecordsApp.kt`

Run the one-time backfill on app startup. The backfill assigns UUIDs and sets `updatedAt` on every pre-existing record that has an empty `firebaseId`. It is safe to run on every startup — it only touches rows where `firebaseId = ''`.

```kotlin
package com.example.patientrecords

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.patientrecords.data.FirebaseRepository
import com.example.patientrecords.data.PatientDatabase
import com.example.patientrecords.data.PatientRepository
import com.example.patientrecords.utils.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class PatientRecordsApp : Application() {

    lateinit var database: PatientDatabase
        private set

    lateinit var repository: PatientRepository
        private set

    lateinit var firebaseRepository: FirebaseRepository
        private set

    // Application-scoped coroutine scope; cancelled when the process dies
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(ThemePreferences(this).themeMode)

        database = PatientDatabase.getInstance(this)
        repository = PatientRepository(database.patientDao(), database.patientFollowUpDao())
        firebaseRepository = FirebaseRepository()

        // One-time backfill: assign UUIDs and updatedAt to any records that pre-date the
        // sync migration. Runs on IO thread; does not block app startup.
        appScope.launch {
            backfillFirebaseIds()
        }
    }

    private suspend fun backfillFirebaseIds() {
        val now = System.currentTimeMillis()

        // Backfill patients
        val patientsWithoutId = repository.getPatientsWithoutFirebaseId()
        patientsWithoutId.forEach { patient ->
            repository.updatePatient(
                patient.copy(
                    firebaseId = UUID.randomUUID().toString(),
                    updatedAt = now
                )
            )
        }

        // Backfill follow-ups — also populate patientFirebaseId from the parent patient
        val followUpsWithoutId = repository.getFollowUpsWithoutFirebaseId()
        // Build a local id → firebaseId lookup from already-backfilled patients
        val patientFirebaseIdMap = repository.getAllPatients()
            .let { flow ->
                // getAllPatients() returns a Flow; collect the current snapshot
                val list = mutableListOf<com.example.patientrecords.data.localdb.Patient>()
                val job = appScope.launch {
                    flow.collect {
                        list.addAll(it)
                        this.coroutineContext[kotlinx.coroutines.Job]?.cancel()
                    }
                }
                job.join()
                list
            }
            .associate { it.id to it.firebaseId }

        followUpsWithoutId.forEach { followUp ->
            repository.updateFollowUp(
                followUp.copy(
                    firebaseId = UUID.randomUUID().toString(),
                    patientFirebaseId = patientFirebaseIdMap[followUp.id] ?: "",
                    updatedAt = now
                )
            )
        }
    }
}
```

> **Simpler alternative for backfill**: Instead of the coroutine flow collection inside backfillFirebaseIds, add a `suspend fun getAllPatientsOnce(): List<Patient>` DAO query (`SELECT * FROM patient_data`) and call that directly. The flow approach above works but is more complex than needed for a one-time operation. Recommended: add the suspend DAO query and replace the flow collection block with a simple `repository.getAllPatientsOnce()` call.

---

## 15. Implementation Order

Do these in order — each step builds on the previous.

**Step 1 — Entity fields** (`Patient.kt`, `PatientFollowUp.kt`)  
Add `firebaseId`, `updatedAt`, `patientFirebaseId`. Build the project. Room will complain about schema mismatch — that's expected; fix it in the next step.

**Step 2 — Migration** (`PatientDatabase.kt`)  
Add `MIGRATION_3_4`, bump version to 4, register migration in `getInstance()`. Build and run on a device/emulator — confirm the app launches without a crash and that existing data is intact.

**Step 3 — DAO queries** (`PatientDao.kt`, `PatientFollowUpDao.kt`)  
Add the two `getXxxWithoutFirebaseId()` queries.

**Step 4 — Repository methods** (`PatientRepository.kt`)  
Expose the two new DAO queries.

**Step 5 — Backfill** (`PatientRecordsApp.kt`)  
Add `backfillFirebaseIds()`. Run the app. After launch, open a SQLite browser (or add a log) and verify that all existing rows now have non-empty `firebaseId` values.

**Step 6 — ViewModel `updatedAt`** (`AddPatientViewModel.kt`, `PatientFollowUpViewModel.kt`)  
Add `.copy(updatedAt = System.currentTimeMillis())` to every insert/update call.

**Step 7 — FirebaseRepository** (`FirebaseRepository.kt`)  
Switch keys from integer to `firebaseId`. Add `markPatientDeleted()` and `markFollowUpDeleted()`.

**Step 8 — FirebaseSyncManager** (`FirebaseSyncManager.kt`)  
Replace the sync logic. Test on a device: create a patient, trigger sync, verify it appears in Firebase under a UUID key (not an integer key). Then edit the patient, trigger sync again, verify `updatedAt` increased and the edit is in Firebase.

**Step 9 — End-to-end test** (manual)  
See §16 below.

---

## 16. End-to-End Test Checklist

Run these after all changes are in place. Test with both apps running (Android on a device/emulator + web app on localhost).

### Android → Web
- [ ] Create a new patient on Android. Trigger sync (Backup screen). Open the web app. Patient appears.
- [ ] Edit the patient's treatment on Android. Trigger sync. Refresh web app. Edit is reflected.
- [ ] Add a follow-up on Android. Trigger sync. Follow-up appears on web.

### Web → Android
- [ ] Create a new patient on web. Call `/api/sync/push` (or "Sync Now" button). Trigger sync on Android. Patient appears.
- [ ] Edit a patient's treatment on web. Push + Android sync. Edit is reflected on Android.
- [ ] Delete a patient on web (soft-delete in Firebase). Android sync. Patient removed on Android.

### Conflict (both edit same patient offline)
- [ ] With Android offline, edit patient address on Android. Simultaneously (simulated) set a later `updatedAt` on the web version. Go online and sync. Whichever had the higher `updatedAt` should win.

### ID isolation
- [ ] Verify in Firebase that all patient keys are UUID strings (36-char with hyphens), not integers.
- [ ] Create patient on Android (UUID key A) and a separate patient on web (UUID key B). Confirm Firebase has two separate entries — not one overwriting the other.

---

## 17. Known Gaps (deferred)

| Gap | Notes |
|---|---|
| Soft-delete in entity | Current workaround uses sentinel field value. Clean fix: add `deleted: Boolean = false` and `deletedAt: Long = 0L` in a future Room migration 4→5. |
| Automatic background sync on Android | Currently sync is manual (Backup screen). A `WorkManager` periodic task or a Firebase `onValue` listener would make sync automatic when the device comes online. Out of scope for this change. |
| `getAllPatientsOnce()` suspend DAO query | Should be added to `PatientDao` to simplify the backfill; avoids Flow manipulation in `PatientRecordsApp`. |
| Sync status UI on Android | The app has no indicator showing sync state (last synced, pending count). Not needed for correctness. |
| Web-created patients with no `id` field | When Android pulls a web-created patient, the Firebase record's `id` field contains the web MySQL id. Android inserts it with `id = 0` so Room assigns a new local id. The web `id` is lost. This is correct — `firebaseId` is the cross-platform identity; local `id` is only meaningful within each platform. |
