# Codebase — PatientRecords Android App

## Package Structure

```
com.example.patientrecords/
├── PatientRecordsApp.kt                  # Application class / DI container
├── MainActivity.kt                       # Home screen
├── data/
│   ├── PatientDatabase.kt                # Room database singleton
│   ├── PatientRepository.kt              # DAO wrapper / data access
│   ├── FirebaseRepository.kt             # Firebase Realtime DB access
│   ├── FirebaseSyncManager.kt            # Bidirectional sync logic
│   └── localdb/
│       ├── Patient.kt                    # Room entity — patient_data
│       ├── PatientDao.kt                 # Patient queries
│       ├── PatientFollowUp.kt            # Room entity — follow_up_data
│       └── PatientFollowUpDao.kt         # Follow-up queries
├── ui/
│   ├── base/
│   │   └── BaseActivity.kt              # Shared toolbar + drawer base
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── addpatient/
│   │   ├── AddPatientActivity.kt
│   │   ├── AddPatientViewModel.kt
│   │   └── AddPatientViewModelFactory.kt
│   ├── viewallpatient/
│   │   ├── ViewAllPatientsActivity.kt
│   │   ├── ViewAllPatientsViewModel.kt
│   │   ├── ViewAllPatientsViewModelFactory.kt
│   │   └── PatientAdapter.kt
│   ├── patienthistory/
│   │   ├── PatientHistoryActivity.kt
│   │   ├── PatientHistoryViewModel.kt
│   │   └── PatientHistoryViewModelFactory.kt
│   ├── followuppatient/
│   │   ├── PatientFollowUpActivity.kt
│   │   ├── PatientFollowUpViewModel.kt
│   │   └── PatientFollowUpViewModelFactory.kt
│   ├── dashboard/
│   │   ├── DashboardActivity.kt
│   │   └── DashboardViewModel.kt
│   └── backup/
│       ├── BackUpActivity.kt
│       └── BackUpViewModel.kt
└── utils/
    └── Extensions.kt                    # Constants + time helpers + display formatter
```

---

## Application Class

### `PatientRecordsApp.kt`

Manual dependency-injection container. Initialised once when the app process starts.

```kotlin
class PatientRecordsApp : Application() {
    lateinit var repository: PatientRepository     // exposed to all Activities
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate() {
        super.onCreate()
        val database = PatientDatabase.getInstance(this)
        repository = PatientRepository(database.patientDao(), database.patientFollowUpDao())
        firebaseRepository = FirebaseRepository()
    }
}
```

Access pattern in Activities:
```kotlin
val repo = (application as PatientRecordsApp).repository
```

---

## Data Layer

### `PatientDatabase.kt`

Room database singleton. Version 1. Thread-safe via `synchronized` block.

```kotlin
@Database(entities = [Patient::class, PatientFollowUp::class], version = 1)
abstract class PatientDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun patientFollowUpDao(): PatientFollowUpDao
    companion object {
        fun getInstance(context: Context): PatientDatabase { /* synchronized singleton */ }
    }
}
```

No migration strategies defined. Any schema change requires either a written `Migration` object or `fallbackToDestructiveMigration()` (data loss).

---

### `Patient.kt`

Room entity for `patient_data`. All 49 fields defined as Kotlin data class properties.

Notable fields:
- `id: Int = 0` — autoGenerate. **Bypassed** in `AddPatientActivity` which passes `Random.nextInt(100000)`.
- `dateJoined: Long? = System.currentTimeMillis()` — default value set at object construction time. **Overwritten on every update** in `collectPatientFromInput()`.
- `followUp1`–`followUp4: String?` — legacy fields, never populated from UI, never displayed.
- `balance: String?` — inconsistent with `PatientFollowUp.balance: Int`.

---

### `PatientFollowUp.kt`

Room entity for `follow_up_data`.

Notable fields:
- `followUpId: Int = -1` — default value is `-1`, not `0`. Room treats `0` as "auto-assign"; `-1` is persisted as a literal ID unless `OnConflictStrategy.REPLACE` reassigns it. **Bypassed** in `PatientFollowUpActivity` with `Random.nextInt(100000)`.
- `id: Int = -1` — FK to `Patient.id`. No `@ForeignKey` annotation; no referential integrity.
- `date: Long = System.currentTimeMillis()` — always set to now at insert and update; original date is not preserved on edit.
- `balance: Int` — typed as `Int`, but `Patient.balance` is `String`. Financial fields inconsistently typed.

---

### `PatientDao.kt`

```kotlin
@Dao interface PatientDao {
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

    // Searches firstName, middleName, lastName
    @Query("SELECT * FROM patient_data WHERE firstName LIKE '%' || :query || '%' OR ...")
    fun searchPatients(query: String): Flow<List<Patient>>

    // For dashboard time-window counts
    @Query("SELECT * FROM patient_data WHERE dateJoined >= :date")
    suspend fun getPatientsFromDay(date: Long): List<Patient>
}
```

`OnConflictStrategy.REPLACE` on insert means a collision in `id` silently overwrites the existing patient's data rather than failing.

---

### `PatientFollowUpDao.kt`

```kotlin
@Dao interface PatientFollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: PatientFollowUp)

    @Update
    suspend fun updateFollowUp(followUp: PatientFollowUp)

    // Ordered DESC — most recent first
    @Query("SELECT * FROM follow_up_data WHERE id = :patientId ORDER BY follow_up_num DESC")
    fun getFollowUpsForPatient(patientId: Int): Flow<List<PatientFollowUp>>

    @Query("SELECT * FROM follow_up_data")
    suspend fun getAllFollowUps(): List<PatientFollowUp>

    @Query("SELECT * FROM follow_up_data WHERE date >= :date")
    suspend fun getFollowUpsFromDay(date: Long): List<PatientFollowUp>

    // Returns Patient rows that had at least one follow-up in the period
    @Query("""
        SELECT DISTINCT p.*
        FROM patient_data p
        INNER JOIN follow_up_data f ON p.id = f.id
        WHERE f.date >= :date
    """)
    suspend fun getPatientsWithFollowUpsFromDay(date: Long): List<Patient>
}
```

`ORDER BY follow_up_num DESC` on a `TEXT` column uses lexicographic ordering — sorts incorrectly after follow-up 9.

---

### `PatientRepository.kt`

Thin pass-through to DAOs. No business logic.

```kotlin
class PatientRepository(
    private val patientDao: PatientDao,
    private val patientFollowUpDao: PatientFollowUpDao
) {
    // Patient CRUD
    suspend fun insertPatient(patient: Patient) = patientDao.insert(patient)
    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)
    suspend fun deletePatient(patient: Patient) = patientDao.delete(patient)
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAll()
    fun getPatientById(patientId: Int): Flow<Patient> = patientDao.getPatientById(patientId)

    // Follow-up CRUD
    suspend fun addFollowUp(followUp: PatientFollowUp) = patientFollowUpDao.insertFollowUp(followUp)
    suspend fun updateFollowUp(followUp: PatientFollowUp) = patientFollowUpDao.updateFollowUp(followUp)
    fun getFollowUps(patientId: Int): Flow<List<PatientFollowUp>> = patientFollowUpDao.getFollowUpsForPatient(patientId)
    suspend fun getAllFollowUps(): List<PatientFollowUp> = patientFollowUpDao.getAllFollowUps()

    // Search
    fun searchPatients(query: String): Flow<List<Patient>> = patientDao.searchPatients(query)

    // Dashboard
    suspend fun getPatientsFromDay(date: Long): List<Patient> = patientDao.getPatientsFromDay(date)
    suspend fun getFollowUpsFromDay(date: Long): List<PatientFollowUp> = patientFollowUpDao.getFollowUpsFromDay(date)
    suspend fun getPatientWithFollowUpFromDay(date: Long): List<Patient> = patientFollowUpDao.getPatientsWithFollowUpsFromDay(date)
}
```

---

### `FirebaseRepository.kt`

Firebase Realtime Database access. References `patients/` and `patient_follow_ups/` nodes.

```kotlin
class FirebaseRepository {
    private val dbPatientRef = FirebaseDatabase.getInstance().getReference("patients")
    private val dbPatientFollowUpRef = FirebaseDatabase.getInstance().getReference("patient_follow_ups")

    // Upload: per-child setValue — no batching
    fun uploadPatients(patients: List<Patient>) {
        patients.forEach { patient ->
            dbPatientRef.child(patient.id.toString()).setValue(patient)
        }
    }

    // Download: single-event listener in suspendCoroutine
    suspend fun downloadPatients(): List<Patient> = suspendCoroutine { cont ->
        dbPatientRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cont.resume(snapshot.children.mapNotNull { it.getValue(Patient::class.java) })
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resume(emptyList())   // error swallowed
            }
        })
    }
    // Same pattern for uploadPatientFollowUps / downloadFollowUps

    fun sampleData() { /* hardcoded test patient — should be removed */ }
}
```

**Issues**:
- Errors are swallowed in `onCancelled` (resumes with `emptyList()`). Callers cannot distinguish a network failure from a genuinely empty database.
- No batched writes — each record is a separate Firebase network call.
- `sampleData()` is a debug method with a hardcoded test patient (id=132321); never called from production code but is a maintenance hazard.

---

### `FirebaseSyncManager.kt`

Bidirectional merge. Called from `BackUpViewModel` on "Sync to Cloud" button press.

```kotlin
class FirebaseSyncManager(
    private val localRepo: PatientRepository,
    private val firebaseRepo: FirebaseRepository
) {
    suspend fun syncPatientsBothWays() {
        val localPatients  = localRepo.getAllPatients().first()
        val remotePatients = firebaseRepo.downloadPatients()

        val toFirebase = localPatients.filter { local ->
            val remote = remotePatients.find { it.id == local.id }
            remote == null || local != remote &&
                    (local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()
            //      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            //      BUG: Long values compared as strings (lexicographic, not numeric)
        }

        val toRoom = remotePatients.filter { remote ->
            val local = localPatients.find { it.id == remote.id }
            local == null || remote != local &&
                    (local.dateJoined ?: 0).toString() < (remote.dateJoined ?: 0).toString()
        }

        firebaseRepo.uploadPatients(toFirebase)
        toRoom.forEach { localRepo.insertPatient(it) }  // REPLACE on conflict
    }

    suspend fun syncPatientFollowUpsBothWays() { /* same pattern for follow-ups */ }
}
```

**Key bug**: `(local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()` — comparing epoch milliseconds as strings. Works coincidentally for modern 13-digit timestamps but is semantically wrong and fragile.

---

## UI Layer

### `BaseActivity.kt`

Abstract base. All post-login Activities extend this.

```kotlin
abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerToggle: ActionBarDrawerToggle  // BUG: never initialised

    protected fun setChildContentView(childView: View) { baseBinding.baseContainer.addView(childView) }
    protected fun initToolbarWithDrawer() { /* sets up toggle, 50% width drawer */ }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) { return true }  // BUG: NPE — drawerToggle uninitialised
        return super.onOptionsItemSelected(item)
    }

    // Two identical navigation when-blocks — both fire for the same event:
    private fun onDrawerItemSelected(item: MenuItem) { when(item.itemId) { ... } }
    override fun onNavigationItemSelected(item: MenuItem): Boolean { when(item.itemId) { ... }; return true }

    // Logout handler — empty:
    R.id.nav_logout -> { /* TODO */ }
}
```

---

### `LoginActivity.kt` / `LoginViewModel.kt`

```kotlin
// LoginViewModel.kt
fun onLoginClicked() {
    if (_email.value == "a" && _password.value == "a") {
        _loginResult.value = true   // hardcoded credentials — no real auth
    }
}
```

No Firebase Authentication. No session persistence. Credentials are `a` / `a`.

---

### `AddPatientActivity.kt`

Handles three modes determined by Intent extras:

| Mode | Condition | UI state |
|---|---|---|
| Add | `patientId == -1` | Fields enabled; Submit button visible |
| View | `isViewMode == true` | Fields disabled; Edit button visible |
| Edit | Edit button clicked | Fields enabled; Update button visible |

`setPatientFromDb()`, `setViewOnlyMode()`, `enableAllFields()`: each manually call `.isEnabled` / `.setText` on 35 bindings. No loop, no reflection — must be updated manually if fields are added.

`collectPatientFromInput()`:

```kotlin
private fun collectPatientFromInput(): Patient {
    var currPatient = Random.nextInt(100000)   // BUG: collision risk
    if (isEditMode) currPatient = patientId

    return Patient(
        id = currPatient,
        // ...
        dateJoined = System.currentTimeMillis(),  // BUG: overwrites original dateJoined on edit
    )
}
```

---

### `ViewAllPatientsActivity.kt`

RecyclerView with debounced search. Uses `launchWhenStarted` (deprecated):

```kotlin
lifecycleScope.launchWhenStarted {       // deprecated — use repeatOnLifecycle
    viewModel.filteredPatients.collect { adapter.submitList(it) }
}
```

### `ViewAllPatientsViewModel.kt`

```kotlin
val filteredPatients: StateFlow<List<Patient>> = repository
    .getAllPatients()
    .combine(searchQuery) { patients, query ->
        if (query.isBlank()) patients
        else patients.filter { /* name contains query */ }
    }
    .debounce(300)
    .distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

Server-side search is also available via `PatientDao.searchPatients()` but not used here — filtering happens in-memory after loading all patients.

---

### `PatientAdapter.kt`

`ListAdapter<Patient, PatientAdapter.PatientViewHolder>` with `DiffCallback` on `id`. Displays name, sex, occupation, phone, regno. Click triggers `onPatientClickListener` lambda.

---

### `PatientHistoryActivity.kt`

Loads patient summary + all follow-ups. Dynamically inflates `ItemFollowupEntryBinding` per follow-up inside a `ScrollView`. Includes an inline `dpToPx()` helper — this should be in `Extensions.kt`.

---

### `PatientFollowUpActivity.kt`

Three-mode activity matching `AddPatientActivity`. Key issues in `collectPatientFollowUpFromInput()`:

```kotlin
weight = binding.etWeight.text.toString().toInt(),          // BUG: crashes on empty/non-numeric
balance = binding.etBalanceAmount.text.toString().toInt()   // BUG: crashes on empty/non-numeric

var currFollowUpId = Random.nextInt(100000)                  // BUG: collision risk
var currFollowUpNo = (totalInitialFollowUps + 1).toString() // BUG: count-based numbering
                                                            //      breaks if any follow-up deleted
date = System.currentTimeMillis()                           // BUG: date always overwritten on edit
```

`findCurrentFollowup()`: linear search through all follow-ups by `follow_up_num` (String equality). O(n) but acceptable for expected data volumes.

---

### `DashboardActivity.kt`

- Calls `viewModel.loadDashboardData()` and `viewModel.loadSummaryData()` in `onCreate`.
- Observes 8 LiveData counts + 2 StateFlows.
- Dynamically inflates patient list items.
- Real-time clock via `Handler.postDelayed` recurring every 1000ms.
- `DashboardViewModel` accepts `FirebaseRepository` but does not use it — placeholder for a future "sync from dashboard" button.

---

### `BackUpActivity.kt` / `BackUpViewModel.kt`

```kotlin
// BackUpViewModel
fun syncPatients() {
    viewModelScope.launch { syncManager.syncPatientsBothWays() }
}
fun syncPatientFollowUps() {
    viewModelScope.launch { syncManager.syncPatientFollowUpsBothWays() }
}
```

Both coroutines launched independently — no sequencing, no error handling, success reported only via Toast.

---

## Utilities

### `Extensions.kt`

```kotlin
// Intent extra keys
const val EXTRA_PATIENT_ID      = "patient_id"
const val EXTRA_REG_NO          = "patient_reg_no"
const val EXTRA_FOLLOW_UP_NUMBER = "follow_up_number"
const val EXTRA_VIEW_MODE       = "is_view_mode"

// Time helpers (all return epoch ms N days ago)
fun get1DayAgo(): Long
fun get7DaysAgo(): Long
fun get31DaysAgo(): Long
fun get365DaysAgo(): Long

// Display formatter
fun Long.toDisplayDateTime(): String  // → "dd-MM-yyyy HH:mm:ss"
```

Note: `EXTRA_REG_NO` is defined but never used by any Activity.

---

## Testing

| Test type | Location | Status |
|---|---|---|
| Unit tests | `src/test/java/.../ExampleUnitTest.kt` | Placeholder only |
| Instrumented tests | `src/androidTest/.../ExampleInstrumentedTest.kt` | Placeholder only |
| DAO tests | — | Not written |
| ViewModel tests | — | Not written |
| UI/Espresso tests | — | Not written |

Zero business logic test coverage.
