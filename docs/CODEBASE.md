# Codebase — Hospital Data Web App

## Repository Structure

```
Hospital-Data-Web-App/
├── docs/                              # Project documentation (this directory)
├── PatientRecords/                    # Primary Android app (Kotlin, MVVM)
│   └── app/src/main/java/com/example/patientrecords/
│       ├── PatientRecordsApp.kt
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── PatientDatabase.kt
│       │   ├── PatientRepository.kt
│       │   ├── FirebaseRepository.kt
│       │   ├── FirebaseSyncManager.kt
│       │   └── localdb/
│       │       ├── Patient.kt
│       │       ├── PatientDao.kt
│       │       ├── PatientFollowUp.kt
│       │       └── PatientFollowUpDao.kt
│       ├── ui/
│       │   ├── base/BaseActivity.kt
│       │   ├── login/
│       │   │   ├── LoginActivity.kt
│       │   │   └── LoginViewModel.kt
│       │   ├── addpatient/
│       │   │   ├── AddPatientActivity.kt
│       │   │   ├── AddPatientViewModel.kt
│       │   │   └── AddPatientViewModelFactory.kt
│       │   ├── viewallpatient/
│       │   │   ├── ViewAllPatientsActivity.kt
│       │   │   ├── ViewAllPatientsViewModel.kt
│       │   │   ├── ViewAllPatientsViewModelFactory.kt
│       │   │   └── PatientAdapter.kt
│       │   ├── patienthistory/
│       │   │   ├── PatientHistoryActivity.kt
│       │   │   ├── PatientHistoryViewModel.kt
│       │   │   └── PatientHistoryViewModelFactory.kt
│       │   ├── followuppatient/
│       │   │   ├── PatientFollowUpActivity.kt
│       │   │   ├── PatientFollowUpViewModel.kt
│       │   │   └── PatientFollowUpViewModelFactory.kt
│       │   ├── dashboard/
│       │   │   ├── DashboardActivity.kt
│       │   │   └── DashboardViewModel.kt
│       │   └── backup/
│       │       ├── BackUpActivity.kt
│       │       └── BackUpViewModel.kt
│       └── utils/
│           └── Extensions.kt
├── Mahajan Homeo Clinic/              # Legacy Java prototype (auth only)
│   └── app/src/main/java/com/example/loginapp/
│       ├── MainActivity.java
│       ├── RegisterActivity.java
│       ├── VerifyActivity.java
│       ├── DashboardActivity.java
│       ├── BaseActivity.java
│       ├── Clinic.java
│       ├── User.java
│       ├── ProductGridFragment.java
│       └── NavigationIconClickListener.java
└── Web-App/                           # PHP + MySQL admin interface
    ├── index.php
    ├── home.php
    ├── dashboard.php
    ├── records.php
    ├── fillForm.php
    ├── followUp.php
    ├── patientDetails.php
    ├── patientDetailsEdit.php
    ├── detailedRecords.php
    ├── search.php
    ├── updateRecord.php
    ├── includes/
    │   ├── connection.php
    │   ├── header.php
    │   ├── logincheck.php
    │   ├── earning.php
    │   ├── filter.php
    │   ├── clock.php
    │   ├── recordCard.php
    │   ├── patientDetailsForm.php
    │   ├── detailsCard.php
    │   ├── fillFormSideNav.php
    │   ├── followUpForm.php
    │   └── footer.php
    ├── php/
    │   ├── insertRecord.php
    │   ├── insertFollowUp.php
    │   ├── updateRecord.php
    │   └── deleteRecord.php
    ├── css/style.css
    └── js/script.js
```

---

## PatientRecords — Kotlin Android App

### `PatientRecordsApp.kt`
**Package**: `com.example.patientrecords`

Application subclass used as a manual dependency-injection container.

```kotlin
class PatientRecordsApp : Application() {
    lateinit var repository: PatientRepository
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate() {
        val database = PatientDatabase.getInstance(this)
        repository = PatientRepository(database.patientDao(), database.patientFollowUpDao())
        firebaseRepository = FirebaseRepository()
    }
}
```

All Activities access the repositories via `(application as PatientRecordsApp).repository`.

---

### Data Layer

#### `PatientDatabase.kt`
Room database singleton. Version 1. Declares entities `Patient` and `PatientFollowUp`. Uses `synchronized` block for thread-safe singleton creation.

#### `Patient.kt`
Room entity mapped to `patient_data` table. 49 fields. Dates stored as `Long` (epoch milliseconds). `dateJoined` defaults to `System.currentTimeMillis()` at object construction. The `id` uses `autoGenerate = true` but `AddPatientActivity` bypasses this by passing `Random.nextInt(100000)` as the id.

#### `PatientFollowUp.kt`
Room entity for `follow_up_data`. `followUpId` defaults to `-1` (not `0`) — this is unusual for an auto-generated key and can cause conflicts. `balance` is stored as `Int`, while `paid` is stored as `String` — inconsistent typing for financial fields.

#### `PatientDao.kt`
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(patient: Patient)
@Delete suspend fun delete(patient: Patient)
@Update suspend fun updatePatient(patient: Patient)
fun getAll(): Flow<List<Patient>>
fun getPatientById(patientId: Int): Flow<Patient>
fun searchPatients(query: String): Flow<List<Patient>>  // searches firstName, middleName, lastName
suspend fun getPatientsFromDay(date: Long): List<Patient>
```

#### `PatientFollowUpDao.kt`
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFollowUp(followUp: PatientFollowUp)
@Update suspend fun updateFollowUp(followUp: PatientFollowUp)
fun getFollowUpsForPatient(patientId: Int): Flow<List<PatientFollowUp>>  // ordered DESC
suspend fun getAllFollowUps(): List<PatientFollowUp>
suspend fun getFollowUpsFromDay(date: Long): List<PatientFollowUp>
// INNER JOIN query returning Patient rows that had a follow-up in the period:
suspend fun getPatientsWithFollowUpsFromDay(date: Long): List<Patient>
```

#### `PatientRepository.kt`
Thin wrapper around both DAOs. All patient CRUD is suspend + Flow. Dashboard methods (`getPatientsFromDay`, `getFollowUpsFromDay`, `getPatientWithFollowUpFromDay`) return plain Lists for one-shot queries.

#### `FirebaseRepository.kt`
- References `patients/` and `patient_follow_ups/` in Firebase Realtime DB.
- `uploadPatients` / `uploadPatientFollowUps`: fire-and-forget; uses per-child `setValue()`. No batch write.
- `downloadPatients` / `downloadFollowUps`: single-event listener wrapped in `suspendCoroutine`.
- Cancellation handling: on `onCancelled`, resumes with `emptyList()` — no error propagation.
- `sampleData()`: debug method with hardcoded test patient; should be removed before production.

#### `FirebaseSyncManager.kt`
Bidirectional sync invoked manually from `BackUpActivity`.

```
syncPatientsBothWays():
  1. Load all from Room (Flow.first())
  2. Download all from Firebase
  3. toFirebase = items where remote is null OR timestamps indicate local is newer
  4. toRoom   = items where local is null OR timestamps indicate remote is newer
  5. Upload toFirebase, insert toRoom

syncPatientFollowUpsBothWays(): same pattern for follow_ups
```

**Known bug**: Timestamp comparison is done with `.toString()` converting `Long` values to strings and then using string comparison (`>`, `<`). For epoch milliseconds this works as long as all values have the same digit count (they do for current timestamps), but it is semantically wrong and will silently break for timestamps before year 2001 (10 digits) vs current (13 digits).

---

### UI Layer

#### `BaseActivity.kt`
Abstract base for all post-login activities.

- Inflates `activity_base.xml` (DrawerLayout with `MaterialToolbar` and `NavigationView`).
- `setChildContentView(view)`: adds child content into `baseContainer` FrameLayout.
- `initToolbarWithDrawer()`: sets up `ActionBarDrawerToggle`, white icon tint, 50% screen width drawer.
- `onDrawerItemSelected()` and `onNavigationItemSelected()`: duplicate identical `when` blocks — both should delegate to a single handler.
- **Bug**: `drawerToggle` (secondary ActionBarDrawerToggle field) is declared but never initialised; `onOptionsItemSelected` calls `drawerToggle.onOptionsItemSelected()` which will throw `UninitializedPropertyAccessException` if triggered.
- Logout menu item (`R.id.nav_logout`): handler body is empty — no logout logic implemented.
- `applyTopPaddingToRoot()`: edge-to-edge status bar inset handling via `ViewCompat.setOnApplyWindowInsetsListener`.

#### `LoginActivity.kt` / `LoginViewModel.kt`
- Hardcoded credentials: email `"a"`, password `"a"`.
- On success: navigates to `MainActivity`.
- No Firebase Authentication, no session persistence.

#### `MainActivity.kt`
Home screen with two buttons: **View Patient Records** → `ViewAllPatientsActivity`, **Add Patient** → `AddPatientActivity`. Extends `BaseActivity`.

#### `AddPatientActivity.kt`
Handles three modes: **Add** (patientId == -1), **View** (isViewMode = true), **Edit** (Edit button click).

- `setPatientFromDb(patient)`: populates all 35+ EditText fields from a `Patient` object.
- `setViewOnlyMode()` / `enableAllFields()`: manually sets `isEnabled` on every field (no loop/reflection — brittle if fields are added).
- `collectPatientFromInput()`: reads all fields and builds a `Patient`. Sets `id = Random.nextInt(100000)` for new patients instead of relying on `autoGenerate`. This can produce collisions.
- On edit: uses `patientId` from intent extras correctly.
- `dateJoined` is always set to `System.currentTimeMillis()` on collect — even on update, which overwrites the original join date.

#### `ViewAllPatientsActivity.kt`
- RecyclerView backed by `PatientAdapter` (ListAdapter with DiffCallback).
- Search: `StateFlow` with `debounce(300)` and `distinctUntilChanged`.
- Lifecycle: `launchWhenStarted` (deprecated in newer lifecycle versions; should migrate to `repeatOnLifecycle(STARTED)`).

#### `PatientAdapter.kt`
- Displays: Name, Sex, Occupation, Phone, RegNo.
- Click navigates to `PatientHistoryActivity` with `EXTRA_PATIENT_ID`.

#### `PatientHistoryActivity.kt`
- Loads patient summary card + all follow-ups for that patient.
- Follow-up items dynamically inflated into a ScrollView using `ItemFollowupEntryBinding`.
- Each follow-up row has a click listener navigating to `PatientFollowUpActivity` in view mode, passing `EXTRA_FOLLOW_UP_NUMBER`.
- Helper `dpToPx()` defined inline — should be in a utility file.

#### `PatientFollowUpActivity.kt`
Handles three modes: **Add** (patientFollowUpNumber == "-1"), **View**, **Edit**.

- `findCurrentFollowup()`: linear search through all follow-ups for the patient by `follow_up_num` (String comparison).
- `collectPatientFollowUpFromInput()`:
  - `weight`: `binding.etWeight.text.toString().toInt()` — crashes on empty input (no `toIntOrNull()`).
  - `balance`: `binding.etBalanceAmount.text.toString().toInt()` — same crash risk.
  - `followUpId = Random.nextInt(100000)` for new follow-ups — same collision risk as patients.
  - `date` always set to `System.currentTimeMillis()` on both insert and update.

#### `DashboardActivity.kt`
- Displays real-time clock (updated every second via `Handler.postDelayed`).
- Observes 8 LiveData counts and 2 StateFlows.
- Dynamically inflates `ItemPatientBinding` for "Patients last week" and "Patients with Follow-ups last week" lists.
- Calls `viewModel.loadDashboardData()` and `viewModel.loadSummaryData()` in `onCreate`.

#### `DashboardViewModel.kt`
- 8 `MutableLiveData<Int>` for count cards (today/week/month/year × patients/followups).
- 2 `MutableStateFlow<List<Patient>>` for the scrollable lists.
- `FirebaseRepository` injected but unused — placeholder for future sync trigger from dashboard.

#### `BackUpActivity.kt` / `BackUpViewModel.kt`
- Single "Sync to Cloud" button.
- `BackUpViewModel.syncPatients()` and `syncPatientFollowUps()` launch separate coroutines calling `FirebaseSyncManager`.
- No progress indicator; no error feedback beyond a Toast.

#### `Extensions.kt`
- Constants: `EXTRA_PATIENT_ID`, `EXTRA_REG_NO`, `EXTRA_FOLLOW_UP_NUMBER`, `EXTRA_VIEW_MODE`.
- Time helpers: `get1DayAgo()`, `get7DaysAgo()`, `get31DaysAgo()`, `get365DaysAgo()` — returns epoch millis for N days ago using `Calendar`.
- Extension function: `Long.toDisplayDateTime()` → `"dd-MM-yyyy HH:mm:ss"` via `SimpleDateFormat`.

---

## Mahajan Homeo Clinic — Java Legacy App

### `MainActivity.java`
Firebase Auth login. Validates non-empty email and password (≥8 chars). Calls `signInWithEmailAndPassword`. On success, checks email verification. If verified, starts `DashboardActivity`. `AuthStateListener` re-checks on resume.

Public static constant `userEmail = ""` used as Intent extra key — a string key named `userEmail` is semantically confusing.

### `RegisterActivity.java`
Builds a `User` object (Displayname, Email, createdAt timestamp). Calls `createUserWithEmailAndPassword` then stores the user in `Users/` Firebase node. Starts phone verification via `VerifyActivity`. Sends email verification.

**Critical**: Password string is stored to Firebase DB alongside the user record in plaintext.

### `VerifyActivity.java`
Firebase Phone Auth. Country code hardcoded as `"+91"` (India). 60-second OTP timeout. On successful verification, shows message "Request forwarded to Admin for approval" and returns to login — admin approval workflow is not actually implemented anywhere.

### `User.java`
Simple data class with `Displayname`, `Email`, `createdAt`. Getter-only; no setter; uses a parameterised constructor. Firebase requires a no-arg constructor — one is present.

### `BaseActivity.java`
Abstract base with toolbar setup. Uses `Clinic` app class to track the current foreground activity. Contains a `Toast.makeText(this, "BaseActivity Created", ...)` debug toast that fires on every `BaseActivity.onCreate()` — should be removed.

### `DashboardActivity.java`
Stub implementation. Receives email via intent. No patient data functionality.

---

## Web-App — PHP + MySQL

### `includes/connection.php`
```php
$conn = mysqli_connect("localhost", "root", "", "hospital");
```
No password on the database root user. Error mode not configured (errors echo raw SQL on failure).

### `includes/logincheck.php`
Prepared statement lookup by `email_admin OR uid_admin`. Direct plaintext password comparison:
```php
if ($password != $row['password_admin'])
```
`session_start()` is called only after a successful login check; it should be called at the top of the file to avoid header issues.

Email sanitisation and validation code exists in the file but is commented out.

### `includes/earning.php`
Four time-period calculations (today / 7 / 30 / 365 days) for new patients, follow-ups, and SUM(paid). Uses `DATE_SUB(CURDATE(), INTERVAL N DAY)` with hardcoded intervals — not vulnerable to injection, but `SUM(paid)` on a `VARCHAR` column silently converts non-numeric strings to 0.

### `php/insertRecord.php`
Reads all 40+ patient fields from `$_GET` parameters and builds a single SQL INSERT string with direct string interpolation:
```php
$sql = "INSERT INTO patient_data (...) VALUES ('$firstName', '$lastName', ...)";
```
Every field is directly injectable. No input sanitisation. No prepared statements.

### `php/insertFollowUp.php`
Calculates `follow_up_num` by running:
```sql
SELECT MAX(follow_up_num) FROM follow_up_data WHERE id = '$id'
```
Then increments by 1. Same SQL injection vulnerability as `insertRecord.php`. `MAX()` on a `VARCHAR` column performs lexicographic comparison — "9" > "10" — causing wrong follow-up numbering after 9 entries.

### `php/updateRecord.php`
Same structure as `insertRecord.php` but builds an UPDATE query. Direct string interpolation; no prepared statements.

### `php/deleteRecord.php`
Deletes from `patient_data` only. No corresponding delete of related `follow_up_data` rows — orphaned records accumulate.

### `includes/filter.php`
Builds ORDER BY clause from the `sort` query parameter without whitelisting:
```php
$sql .= "ORDER BY $sort ";
```
While this particular column is selected from a dropdown client-side, the server trusts the client value directly — an attacker can inject arbitrary SQL through the `sort` parameter.

### `patientDetails.php`
Loads patient by ID from a GET parameter:
```php
$sql = "SELECT * FROM patient_data WHERE id = '".$_GET['id']."'";
```
Classic SQL injection. Output is echoed directly to HTML without `htmlspecialchars` — XSS risk.

### `js/script.js`
Annyang + SpeechKITT integration. Defines voice commands for form field population (e.g., `"first name *val"` → sets `#firstName` input). Auto-restarts on end of speech session. Commands: `hello`, `what is this`, `reload the page`, `stop listening`, `submit`, plus per-field commands for all patient form fields.

---

## Build Configuration

### PatientRecords (`app/build.gradle.kts`)
- `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`
- `versionCode = 1`, `versionName = "1.0"`
- ViewBinding + DataBinding both enabled
- Room `2.6.1` with KSP annotation processing
- Firebase BOM via `google-services` plugin
- No ProGuard/R8 enabled on release builds (`isMinifyEnabled = false`)

### Mahajan Homeo Clinic (`app/build.gradle`)
- Groovy DSL (legacy format)
- Firebase Auth + Realtime Database
- Material Components

---

## Testing

### PatientRecords
- `ExampleUnitTest.kt`: placeholder `addition_isCorrect` test only.
- `ExampleInstrumentedTest.kt`: placeholder instrumentation test only.
- No business logic tests; no DAO tests; no ViewModel tests.

### Mahajan Homeo Clinic
- `ExampleUnitTest.java`: placeholder only.
- `ExampleInstrumentedTest.java`: placeholder only.

No test coverage exists in either app.
