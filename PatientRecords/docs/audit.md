# Audit Report — PatientRecords Android App

**Date**: 2026-05-23  
**Scope**: Complete Kotlin Android codebase  
**Severity**: `[CRITICAL]` `[HIGH]` `[MEDIUM]` `[LOW]`

---

## Summary

| Severity | Count |
|---|---|
| CRITICAL | 1 |
| HIGH | 7 |
| MEDIUM | 6 |
| LOW | 4 |
| **Total** | **18** |

---

## CRITICAL Issues

---

### AND-SEC-01 `[CRITICAL]` Hardcoded Credentials — No Real Authentication

**Files**: `ui/login/LoginViewModel.kt`, `ui/login/LoginActivity.kt`

**Description**  
The entire app is gated behind a login screen that accepts only the hardcoded values `email = "a"` and `password = "a"`:

```kotlin
// LoginViewModel.kt
fun onLoginClicked() {
    if (_email.value == "a" && _password.value == "a") {
        _loginResult.value = true
    }
}
```

Any person who obtains the APK can log in and access all patient health records. Firebase Authentication is already a declared dependency in `build.gradle.kts` and is fully implemented in the companion "Mahajan Homeo Clinic" project — there is no technical barrier to using it here.

**Fix Plan**  
1. Add `implementation(platform("com.google.firebase:firebase-bom:..."))` + `firebase-auth-ktx` to `app/build.gradle.kts`.
2. Replace `LoginViewModel.onLoginClicked()`:
   ```kotlin
   fun onLoginClicked() {
       val auth = FirebaseAuth.getInstance()
       auth.signInWithEmailAndPassword(_email.value.orEmpty(), _password.value.orEmpty())
           .addOnSuccessListener { _loginResult.value = true }
           .addOnFailureListener { e -> _loginError.value = e.message }
   }
   ```
3. On `LoginActivity` start, check `FirebaseAuth.getInstance().currentUser != null` and skip to `MainActivity` if already signed in.
4. Implement logout (see AND-HIGH-01).

---

## HIGH Issues

---

### AND-HIGH-01 `[HIGH]` Logout Handler is Empty

**File**: `ui/base/BaseActivity.kt` (line 99 and line 126)

**Description**  
The navigation drawer has a "Logout" item. Both handler methods (`onDrawerItemSelected` and `onNavigationItemSelected`) reach the logout case but do nothing:

```kotlin
R.id.nav_logout -> {
    // Handle Logout
}
```

A user who taps Logout sees the drawer close and nothing else — they remain fully logged in.

**Fix**  
```kotlin
R.id.nav_logout -> {
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}
```

---

### AND-HIGH-02 `[HIGH]` `toInt()` Crash on Empty or Non-Numeric Input

**File**: `ui/followuppatient/PatientFollowUpActivity.kt` (lines 170, 176)

**Description**  
When the user submits the follow-up form, two fields are parsed without null/format safety:

```kotlin
weight  = binding.etWeight.text.toString().toInt(),          // line 170
balance = binding.etBalanceAmount.text.toString().toInt()    // line 176
```

`toInt()` throws `NumberFormatException` if either field is empty or contains non-numeric text (e.g., `"5 kg"`). The app crashes with no user-visible error message.

**Fix**  
Replace with `toIntOrNull()` and add field validation before submission:

```kotlin
val weight = binding.etWeight.text.toString().toIntOrNull()
val balance = binding.etBalanceAmount.text.toString().toIntOrNull()

if (weight == null) {
    binding.etWeight.error = "Enter a valid number"
    return
}
if (balance == null) {
    binding.etBalanceAmount.error = "Enter a valid number"
    return
}
```

Then pass the validated non-null values into `PatientFollowUp(...)`.

---

### AND-HIGH-03 `[HIGH]` Random ID Generation Causes Silent Data Overwrite

**Files**: `ui/addpatient/AddPatientActivity.kt` (line 204), `ui/followuppatient/PatientFollowUpActivity.kt` (line 156)

**Description**  
New patients and follow-ups are assigned IDs via `Random.nextInt(100000)`:

```kotlin
var currPatient   = Random.nextInt(100000)   // AddPatientActivity
var currFollowUpId = Random.nextInt(100000)   // PatientFollowUpActivity
```

Room's `@Insert(onConflict = OnConflictStrategy.REPLACE)` means a collision silently **overwrites** an existing patient's entire record with the new patient's data. With a 100,000-value space, the birthday-paradox probability of a collision exceeds 50% after approximately **400 records**.

**Fix**  
Set `id = 0` for new inserts. Room interprets `0` on an `autoGenerate` column as "assign next available ID":

```kotlin
// AddPatientActivity.collectPatientFromInput()
return Patient(
    id = if (isEditMode) patientId else 0,
    ...
)

// PatientFollowUpActivity.collectPatientFollowUpFromInput()
return PatientFollowUp(
    followUpId = if (isEditMode) followUpId else 0,
    ...
)
```

Also change `PatientFollowUp`'s default `followUpId = -1` to `followUpId = 0` in the data class to align with Room's autoGenerate semantics.

---

### AND-HIGH-04 `[HIGH]` `dateJoined` Overwritten on Patient Edit

**File**: `ui/addpatient/AddPatientActivity.kt` (line 244)

**Description**  
`collectPatientFromInput()` always sets:

```kotlin
dateJoined = System.currentTimeMillis()
```

This is called for both new inserts and updates. When a doctor edits a patient's occupation or treatment, the patient's original registration date is replaced with the edit timestamp. Consequences:
- The patient appears as a "new patient this week" on the dashboard after any edit.
- Historical date data is permanently lost with no recovery path.

**Fix**  
Preserve the original `dateJoined` in edit mode by caching it from the loaded patient:

```kotlin
// In AddPatientActivity:
private var originalDateJoined: Long? = null

// In setPatientFromDb():
originalDateJoined = patient.dateJoined

// In collectPatientFromInput():
dateJoined = if (isEditMode) originalDateJoined else System.currentTimeMillis()
```

---

### AND-HIGH-05 `[HIGH]` String-Based Timestamp Comparison in Sync Manager

**File**: `data/FirebaseSyncManager.kt` (lines 26, 33, 50, 57)

**Description**  
The bidirectional sync logic determines which copy of a record is newer by comparing `Long` timestamps as strings:

```kotlin
(local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()
```

String comparison on numbers is lexicographic. For numbers of different digit counts: `"9" > "10"` is `true` (because `'9' > '1'`), which is the opposite of the correct numeric result.

Current 13-digit epoch timestamps (`1700000000000`) happen to compare correctly because they all have the same digit count — but this is an accident of the current time period, not a correctness guarantee. Any record with a `dateJoined` of `0` (the nullable fallback) compared against a real timestamp will produce wrong direction decisions, causing records to be synced in the wrong direction.

**Fix**  
Remove `.toString()` — compare as `Long` directly:

```kotlin
// syncPatientsBothWays:
val toFirebase = localPatients.filter { local ->
    val remote = remotePatients.find { it.id == local.id }
    remote == null || (local != remote && (local.dateJoined ?: 0L) > (remote.dateJoined ?: 0L))
}

val toRoom = remotePatients.filter { remote ->
    val local = localPatients.find { it.id == remote.id }
    local == null || (remote != local && (remote.dateJoined ?: 0L) > (local.dateJoined ?: 0L))
}
```

Apply the same fix for `PatientFollowUp.date` in `syncPatientFollowUpsBothWays()`.

---

### AND-HIGH-06 `[HIGH]` `drawerToggle` Field Never Initialised — Crash Risk

**File**: `ui/base/BaseActivity.kt` (lines 35, 143)

**Description**  
Two `ActionBarDrawerToggle` fields are declared: `toggle` (initialised in `initToolbarWithDrawer()`) and `drawerToggle` (never initialised). `onOptionsItemSelected` calls the uninitialised field:

```kotlin
private lateinit var drawerToggle: ActionBarDrawerToggle  // NEVER initialised

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (drawerToggle.onOptionsItemSelected(item)) { return true }  // UninitializedPropertyAccessException
    return super.onOptionsItemSelected(item)
}
```

This crash is triggered by any `onOptionsItemSelected` event on any screen that extends `BaseActivity`.

**Fix**  
Remove `drawerToggle` and use `toggle` everywhere:

```kotlin
// Remove: private lateinit var drawerToggle: ActionBarDrawerToggle

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (toggle.onOptionsItemSelected(item)) { return true }
    return super.onOptionsItemSelected(item)
}
```

---

### AND-HIGH-07 `[HIGH]` Firebase Sync Errors Silently Swallowed

**File**: `data/FirebaseRepository.kt` (lines 38–41, 59–62)

**Description**  
When a Firebase download is cancelled or fails (network error, permission denied, etc.), `onCancelled` resumes with an empty list:

```kotlin
override fun onCancelled(error: DatabaseError) {
    cont.resume(emptyList())   // error is ignored
}
```

`FirebaseSyncManager` receives `emptyList()` for the remote data and interprets it as "Firebase is empty, upload everything local." This causes the entire local database to be uploaded to Firebase on every sync failure, even if Firebase already has current data — and `toRoom` will be empty so no download happens. The sync silently does the wrong thing with no user notification.

**Fix**  
Use `suspendCancellableCoroutine` and resume with an exception on cancellation:

```kotlin
suspend fun downloadPatients(): List<Patient> = suspendCancellableCoroutine { cont ->
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            cont.resume(snapshot.children.mapNotNull { it.getValue(Patient::class.java) })
        }
        override fun onCancelled(error: DatabaseError) {
            cont.resumeWithException(error.toException())
        }
    }
    dbPatientRef.addListenerForSingleValueEvent(listener)
    cont.invokeOnCancellation { dbPatientRef.removeEventListener(listener) }
}
```

In `FirebaseSyncManager`, wrap the call in a `try/catch` and surface the error to the UI:

```kotlin
try {
    val remotePatients = firebaseRepo.downloadPatients()
    // ... sync logic
} catch (e: Exception) {
    // propagate to ViewModel → LiveData → UI
    throw e
}
```

---

## MEDIUM Issues

---

### AND-MED-01 `[MEDIUM]` No Foreign Key Constraint on `follow_up_data.id`

**File**: `data/localdb/PatientFollowUp.kt`

**Description**  
`PatientFollowUp.id` references `Patient.id` but there is no `@ForeignKey` annotation. Room will not enforce referential integrity. If a patient is deleted (once deletion UI is added) or if a sync inserts a follow-up for a non-existent patient, the orphaned rows remain silently in the database and are never surfaced or cleaned up.

**Fix**  
Add the foreign key annotation and increment the database version:

```kotlin
@Entity(
    tableName = "follow_up_data",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE   // auto-delete follow-ups when patient deleted
    )],
    indices = [Index(value = ["id"])]   // required for FK in Room
)
data class PatientFollowUp(...)
```

Write a Room migration:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate the table with the FK constraint
    }
}
```

---

### AND-MED-02 `[MEDIUM]` Follow-up Numbering Breaks When Follow-ups Are Deleted

**File**: `ui/followuppatient/PatientFollowUpActivity.kt` (line 157)

**Description**  
New follow-up numbers are assigned as `totalInitialFollowUps + 1`, where `totalInitialFollowUps` is the count of current follow-ups for the patient:

```kotlin
var currFollowUpNo = (totalInitialFollowUps + 1).toString()
```

If follow-ups 1, 2, and 3 exist and follow-up 2 is deleted (once deletion is implemented), the count becomes 2 and the next insert creates follow-up number `3` — a duplicate of the existing follow-up 3.

Additionally, `ORDER BY follow_up_num DESC` in `PatientFollowUpDao` is on a `TEXT` column — lexicographic ordering means follow-up `"9"` sorts after `"10"`.

**Fix**  
Query the maximum follow-up number instead of using count:

```kotlin
// Add to PatientFollowUpDao:
@Query("SELECT MAX(CAST(follow_up_num AS INTEGER)) FROM follow_up_data WHERE id = :patientId")
suspend fun getMaxFollowUpNum(patientId: Int): Int?
```

```kotlin
// In PatientFollowUpActivity:
val maxNum = viewModel.getMaxFollowUpNum(patientId) ?: 0
currFollowUpNo = (maxNum + 1).toString()
```

Also change `follow_up_num` to `Int` in `PatientFollowUp` to enable correct numeric ordering in the DAO query.

---

### AND-MED-03 `[MEDIUM]` Deprecated `launchWhenStarted` Usage

**File**: `ui/viewallpatient/ViewAllPatientsActivity.kt`

**Description**  
```kotlin
lifecycleScope.launchWhenStarted {
    viewModel.filteredPatients.collect { adapter.submitList(it) }
}
```

`launchWhenStarted` is deprecated. It suspends the coroutine when the lifecycle drops below STARTED but **does not cancel it** — the coroutine continues to hold references and can process emissions when the Activity is in the background, leading to resource leaks and unexpected UI updates on back-navigation.

**Fix**  
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.filteredPatients.collect { adapter.submitList(it) }
    }
}
```

Audit all other Flow collection sites in the codebase for the same pattern.

---

### AND-MED-04 `[MEDIUM]` Duplicate Navigation Handler in BaseActivity

**File**: `ui/base/BaseActivity.kt`

**Description**  
`BaseActivity` contains two separate navigation handler methods with identical `when` blocks:

1. `onDrawerItemSelected(item: MenuItem)` — called from the `setNavigationItemSelectedListener` lambda.
2. `onNavigationItemSelected(item: MenuItem): Boolean` — the `NavigationView.OnNavigationItemSelectedListener` interface method.

Because the `setNavigationItemSelectedListener` lambda in `initToolbarWithDrawer()` calls `onDrawerItemSelected()` AND the class implements the listener interface, both fire when a drawer item is tapped. Each call starts an Activity, potentially launching the same destination twice and creating duplicate Activity instances on the back stack.

**Fix**  
Remove `onDrawerItemSelected()` and route all drawer navigation through `onNavigationItemSelected()`. In `initToolbarWithDrawer()`, replace the lambda with the interface assignment:

```kotlin
navView.setNavigationItemSelectedListener(this)  // routes to onNavigationItemSelected
```

Delete the `onDrawerItemSelected` method entirely.

---

### AND-MED-05 `[MEDIUM]` No Room Migration Strategy

**File**: `data/PatientDatabase.kt`

**Description**  
The database is at version 1 with no `addMigrations()` defined. Any schema change — which is required to fix AND-MED-01 (add FK), AND-LOW-02 (remove legacy fields), or AND-MED-02 (change `follow_up_num` to INT) — will crash the app for existing users unless a migration is provided.

**Fix**  
Before any schema change, define a `Migration` object and register it:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: recreate follow_up_data with FK + INT follow_up_num
        database.execSQL("CREATE TABLE follow_up_data_new (...)")
        database.execSQL("INSERT INTO follow_up_data_new SELECT * FROM follow_up_data")
        database.execSQL("DROP TABLE follow_up_data")
        database.execSQL("ALTER TABLE follow_up_data_new RENAME TO follow_up_data")
    }
}

// In PatientDatabase.getInstance():
Room.databaseBuilder(context, PatientDatabase::class.java, "patient_database")
    .addMigrations(MIGRATION_1_2)
    .build()
```

---

### AND-MED-06 `[MEDIUM]` All Patients Loaded Into Memory for Search

**File**: `ui/viewallpatient/ViewAllPatientsViewModel.kt`

**Description**  
`filteredPatients` loads the entire patient list via `repository.getAllPatients()` and then filters in-memory using Kotlin's `Flow.combine`. `PatientDao.searchPatients()` exists and performs the search at the SQL level but is unused in this ViewModel.

For a clinic with thousands of patients, this will cause memory pressure and slow load times.

**Fix**  
Replace the combine-and-filter approach with the DAO's SQL search:

```kotlin
val filteredPatients: StateFlow<List<Patient>> = searchQuery
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { query ->
        if (query.isBlank()) repository.getAllPatients()
        else repository.searchPatients(query)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

---

## LOW Issues

---

### AND-LOW-01 `[LOW]` `sampleData()` Test Method in Production Class

**File**: `data/FirebaseRepository.kt` (lines 71–88)

**Description**  
A debug method with hardcoded test data (id=132321, firstName="2") exists in the production `FirebaseRepository` class:

```kotlin
fun sampleData() {
    val patient2 = Patient(id = 132321, firstName = "2", middleName = "2", ...)
    patientsRef.child(patient2.id.toString()).setValue(patient2)
}
```

It is never called from production code but remains a maintenance hazard — a developer could accidentally call it, or a future code path could reach it.

**Fix**: Delete the method. If test data is needed, create a `FirebaseRepositoryTest.kt` in the `androidTest` source set.

---

### AND-LOW-02 `[LOW]` Unused Legacy Fields in Patient Entity

**File**: `data/localdb/Patient.kt` (lines 44–47)

**Description**  
`Patient` has four `followUp1`–`followUp4: String?` fields:
- Never set in `AddPatientActivity.collectPatientFromInput()`.
- Never displayed in any UI screen.
- Never queried in any DAO.
- Exist in the Firebase sync payload, adding unnecessary data size.
- Appear to be legacy date fields superseded by the `follow_up_data` table.

**Fix**: Remove the four fields from `Patient` and write a Room migration (version 1→2) to drop the columns. Update `PatientDatabase` version accordingly.

---

### AND-LOW-03 `[LOW]` ProGuard / R8 Disabled on Release Builds

**File**: `app/build.gradle.kts` (line 25)

**Description**  
```kotlin
release {
    isMinifyEnabled = false
}
```

Release APKs contain unobfuscated class names, method names, and string literals. Firebase configuration, data model field names, and application logic are fully readable by decompiling the APK with `jadx` or `apktool`.

**Fix**  
Enable minification and shrinking, and add ProGuard rules to protect Room entities and Firebase data classes from being renamed:

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

`proguard-rules.pro`:
```
# Keep Room entities
-keep class com.example.patientrecords.data.localdb.** { *; }
# Keep Firebase data classes
-keepclassmembers class com.example.patientrecords.data.localdb.** {
    <init>();
}
```

---

### AND-LOW-04 `[LOW]` Zero Test Coverage

**Files**: `src/test/.../ExampleUnitTest.kt`, `src/androidTest/.../ExampleInstrumentedTest.kt`

**Description**  
Both test files contain only the auto-generated `addition_isCorrect` placeholder. There are no tests for:
- DAO queries (especially the INNER JOIN dashboard query and the search query).
- `FirebaseSyncManager` merge logic.
- `ViewAllPatientsViewModel` search filtering and debounce.
- `PatientFollowUpActivity` input collection and crash cases.

Any refactoring or bug fix is unverifiable without running the full app manually.

**Recommended test plan**:

| Test | Type | Priority |
|---|---|---|
| `PatientDao.getPatientsFromDay()` returns correct records | Room in-memory (androidTest) | High |
| `PatientFollowUpDao.getPatientsWithFollowUpsFromDay()` INNER JOIN | Room in-memory (androidTest) | High |
| `PatientDao.searchPatients()` matches all name fields | Room in-memory (androidTest) | Medium |
| `FirebaseSyncManager.syncPatientsBothWays()` — local-only pushes to remote | Unit test with mock repos | High |
| `FirebaseSyncManager` — remote newer record pulled to local | Unit test with mock repos | High |
| `PatientFollowUpActivity` — empty weight field shows error, no crash | Espresso / UI test | High |
| `AddPatientActivity` — submit new patient navigates back | Espresso / UI test | Medium |

---

## Prioritised Fix Roadmap

### Phase 1 — Critical Security

| # | Issue | Files |
|---|---|---|
| 1 | AND-SEC-01: Replace hardcoded login with Firebase Authentication | `LoginViewModel.kt`, `LoginActivity.kt`, `build.gradle.kts` |

### Phase 2 — Crash Fixes and Data Integrity

| # | Issue | Files |
|---|---|---|
| 2 | AND-HIGH-06: Remove uninitialised `drawerToggle` field | `BaseActivity.kt` |
| 3 | AND-HIGH-02: Fix `toInt()` crash in follow-up form | `PatientFollowUpActivity.kt` |
| 4 | AND-HIGH-03: Replace Random IDs with Room autoGenerate | `AddPatientActivity.kt`, `PatientFollowUpActivity.kt`, `PatientFollowUp.kt` |
| 5 | AND-HIGH-04: Preserve `dateJoined` on patient edit | `AddPatientActivity.kt` |
| 6 | AND-HIGH-05: Fix string timestamp comparison in sync | `FirebaseSyncManager.kt` |
| 7 | AND-HIGH-01: Implement logout | `BaseActivity.kt` |
| 8 | AND-HIGH-07: Surface Firebase errors instead of swallowing them | `FirebaseRepository.kt`, `FirebaseSyncManager.kt`, `BackUpViewModel.kt` |

### Phase 3 — Architecture and Data Model

| # | Issue | Files |
|---|---|---|
| 9 | AND-MED-05: Define Room migration strategy (v1→v2) | `PatientDatabase.kt` |
| 10 | AND-MED-01: Add `@ForeignKey` to `PatientFollowUp` (requires migration) | `PatientFollowUp.kt` |
| 11 | AND-LOW-02: Remove unused `followUp1`–`followUp4` fields (same migration) | `Patient.kt` |
| 12 | AND-MED-02: Fix follow-up numbering to use MAX; change type to Int | `PatientFollowUpActivity.kt`, `PatientFollowUp.kt`, `PatientFollowUpDao.kt` |
| 13 | AND-MED-04: Deduplicate navigation handler in BaseActivity | `BaseActivity.kt` |
| 14 | AND-MED-03: Replace `launchWhenStarted` with `repeatOnLifecycle` | `ViewAllPatientsActivity.kt` (and any others) |
| 15 | AND-MED-06: Use SQL-level search instead of in-memory filtering | `ViewAllPatientsViewModel.kt` |

### Phase 4 — Quality

| # | Issue | Files |
|---|---|---|
| 16 | AND-LOW-03: Enable ProGuard/R8 on release | `app/build.gradle.kts`, `proguard-rules.pro` |
| 17 | AND-LOW-01: Remove `sampleData()` debug method | `FirebaseRepository.kt` |
| 18 | AND-LOW-04: Write DAO, ViewModel, and UI tests | `src/test/`, `src/androidTest/` |
