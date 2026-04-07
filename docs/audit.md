# Audit Report — Hospital Data Web App

**Date**: 2026-05-23  
**Scope**: Complete codebase — PatientRecords (Android/Kotlin), Mahajan Homeo Clinic (Android/Java), Web-App (PHP/MySQL)  
**Severity levels**: `[CRITICAL]` `[HIGH]` `[MEDIUM]` `[LOW]`

---

## Summary

| Severity | Count |
|---|---|
| CRITICAL | 5 |
| HIGH | 11 |
| MEDIUM | 9 |
| LOW | 6 |
| **Total** | **31** |

---

## Section 1: Security Issues

### SEC-01 `[CRITICAL]` SQL Injection — Web-App (Multiple Files)

**Files**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`, `patientDetails.php`, `includes/filter.php`

**Description**: User-supplied GET parameters are interpolated directly into SQL strings with no escaping or prepared statements.

```php
// php/insertRecord.php line 49
$sql = "INSERT INTO patient_data (...) VALUES ('$firstName', '$lastName', ...)";

// patientDetails.php line 8
$sql = "SELECT * FROM patient_data WHERE id = '".$_GET['id']."'";

// includes/filter.php
$sql .= "ORDER BY $sort ";  // sort param from HTTP request
```

An attacker can inject arbitrary SQL to read, modify, or delete the entire database.

**Fix**:
1. Convert all SQL operations in `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php` to use MySQLi prepared statements with `bind_param`.
2. In `patientDetails.php`, `records.php`, and all pages that accept an `id` parameter, use a prepared statement.
3. In `includes/filter.php`, validate `$sort` against a whitelist of allowed column names before including it in the query.

```php
// Example fix for insertRecord.php
$stmt = $conn->prepare("INSERT INTO patient_data (firstName, lastName, age ...) VALUES (?, ?, ? ...)");
$stmt->bind_param("ssi ...", $firstName, $lastName, $age ...);
$stmt->execute();
```

---

### SEC-02 `[CRITICAL]` Plaintext Password Storage — Web-App

**File**: `includes/logincheck.php` line 36, `admin_users` table schema

**Description**: Admin passwords are stored in plaintext in the `password_admin` column. The comparison is also plaintext:
```php
if ($password != $row['password_admin'])
```
A single database read exposes all admin credentials.

**Fix**:
1. Hash passwords on storage using `password_hash($password, PASSWORD_BCRYPT)`.
2. Verify using `password_verify($inputPassword, $storedHash)`.
3. Migrate existing admin accounts: prompt admin to reset password on next login or run a one-time migration script.

```php
// Store
$hashed = password_hash($password, PASSWORD_BCRYPT);

// Verify
if (!password_verify($password, $row['password_admin'])) {
    header("Location: ../index.php?error=wrongPassword");
    exit();
}
```

---

### SEC-03 `[CRITICAL]` Plaintext Password in Firebase — Mahajan Homeo Clinic

**File**: `RegisterActivity.java`

**Description**: The user's plaintext password is stored as a field in the Firebase Realtime Database `Users/` node alongside email and display name. Any read access to that node exposes the password.

**Fix**: Remove the password field entirely from the `User` data class and Firebase storage. Firebase Authentication handles credentials; the Realtime DB record should only store profile data (display name, email, phone, organization, timestamps).

---

### SEC-04 `[CRITICAL]` Cross-Site Scripting (XSS) — Web-App

**Files**: `patientDetails.php`, `records.php`, `dashboard.php` (all pages that echo database values)

**Description**: Patient data retrieved from the database is echoed directly into HTML without escaping:
```php
echo $row['firstName'];  // Name could contain <script>alert(1)</script>
```
Any data containing HTML/JS (entered through the unvalidated form) is executed in the browser of anyone viewing the record.

**Fix**: Wrap all database-sourced output in `htmlspecialchars()`:
```php
echo htmlspecialchars($row['firstName'], ENT_QUOTES, 'UTF-8');
```
Consider creating a helper function `h($str)` as a shorthand throughout the project.

---

### SEC-05 `[HIGH]` Hardcoded Credentials — PatientRecords Android

**Files**: `LoginActivity.kt`, `LoginViewModel.kt`

**Description**: The app authenticates against hardcoded values `email = "a"` and `password = "a"`. Any user with the APK can log in. There is no actual authentication.

**Fix**: Replace with Firebase Authentication (already used in the Mahajan Homeo Clinic sub-system). Steps:
1. Add Firebase Auth dependency to `PatientRecords/app/build.gradle.kts`.
2. Update `LoginViewModel.onLoginClicked()` to call `FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)`.
3. Persist auth state across app restarts using `FirebaseAuth.getInstance().currentUser`.
4. Update `BaseActivity` logout handler to call `FirebaseAuth.getInstance().signOut()` then navigate to `LoginActivity`.

---

### SEC-06 `[HIGH]` No Input Validation — Web-App Forms

**Files**: `fillForm.php`, `php/insertRecord.php`, `php/insertFollowUp.php`

**Description**: All form fields accepted via GET with no type, length, or format validation. Numeric fields (`age`, `height`, `weight`, `paid`, `balance`) accept arbitrary strings. Phone numbers accept any format.

**Fix**: Add server-side validation in each PHP endpoint before executing the query:
```php
$age = filter_var($_GET['age'], FILTER_VALIDATE_INT, ['options' => ['min_range' => 0, 'max_range' => 150]]);
if ($age === false) { /* return error */ }

$phone = preg_replace('/[^0-9+]/', '', $_GET['phone']);
```

---

### SEC-07 `[MEDIUM]` Session Started After Output — Web-App

**File**: `includes/logincheck.php` line 41

**Description**: `session_start()` is called inside a conditional block after potential `header()` redirects have already been sent. While this works currently because headers haven't been sent at that point, it is fragile and will break if any output (whitespace, BOM, error) precedes the call.

**Fix**: Move `session_start()` to the very top of `logincheck.php` before any logic.

---

### SEC-08 `[MEDIUM]` No CSRF Protection — Web-App

**File**: All PHP form handlers

**Description**: Forms submit to PHP handlers with no CSRF token. A malicious site can craft a form that POSTs to `includes/logincheck.php` or GETs `php/insertRecord.php` while an admin is logged in.

**Fix**: Generate a CSRF token on form render, store in session, validate on submission:
```php
// On form render
$_SESSION['csrf_token'] = bin2hex(random_bytes(32));

// On form submission
if (!hash_equals($_SESSION['csrf_token'], $_POST['csrf_token'])) {
    die('Invalid CSRF token');
}
```

---

### SEC-09 `[MEDIUM]` Exposed Database Error Details — Web-App

**File**: `php/insertRecord.php` lines 55–56, `includes/connection.php`

**Description**: On SQL errors, raw SQL and `$conn->error` messages are echoed to the browser:
```php
echo "Error: " . $sql . "<br>" . $conn->error;
```
This leaks table structure and query logic to potential attackers.

**Fix**: Log errors server-side with `error_log()` and display a generic user-facing message.

---

## Section 2: Bugs

### BUG-01 `[HIGH]` `toInt()` Crash on Empty Input — PatientFollowUpActivity

**File**: `PatientFollowUpActivity.kt` lines 170, 176

**Description**: When collecting follow-up input:
```kotlin
weight = binding.etWeight.text.toString().toInt(),        // line 170
balance = binding.etBalanceAmount.text.toString().toInt() // line 176
```
`toInt()` throws `NumberFormatException` if the field is empty or contains non-numeric text. The app crashes with no user feedback.

**Fix**: Replace with `toIntOrNull() ?: 0` (or an appropriate default/validation):
```kotlin
weight = binding.etWeight.text.toString().toIntOrNull() ?: 0,
balance = binding.etBalanceAmount.text.toString().toIntOrNull() ?: 0,
```
Also add a validation step before submitting to show an error if mandatory numeric fields are empty.

---

### BUG-02 `[HIGH]` Random ID for New Records Causes Collisions

**Files**: `AddPatientActivity.kt` line 204, `PatientFollowUpActivity.kt` line 156

**Description**: New patients and follow-ups are assigned IDs via `Random.nextInt(100000)`:
```kotlin
var currPatient = Random.nextInt(100000)  // AddPatientActivity
var currFollowUpId = Random.nextInt(100000) // PatientFollowUpActivity
```
The Room DAOs use `OnConflictStrategy.REPLACE` — a collision silently overwrites an existing patient's data. With 100,000 possible values and birthday-paradox probability, a collision is likely after ~400 records.

**Fix**: Remove the manual ID assignment entirely. Room handles auto-increment when the id is `0`:
```kotlin
// In collectPatientFromInput():
return Patient(
    id = 0,  // Room auto-assigns on insert
    ...
)
// In collectPatientFollowUpFromInput():
return PatientFollowUp(
    followUpId = 0,  // Room auto-assigns on insert
    ...
)
```
For edit mode, pass the existing `patientId` / `followUpId` as before.

---

### BUG-03 `[HIGH]` `dateJoined` Overwritten on Patient Update

**File**: `AddPatientActivity.kt` line 244

**Description**: `collectPatientFromInput()` always sets:
```kotlin
dateJoined = System.currentTimeMillis()
```
When this is called during an edit/update, the original join date is replaced with the edit timestamp. A patient who registered 2 years ago will appear as a new patient on today's dashboard after any edit.

**Fix**: Preserve the original `dateJoined` in edit mode:
```kotlin
dateJoined = if (isEditMode) viewModel.patientLiveData.value?.dateJoined else System.currentTimeMillis()
```

---

### BUG-04 `[HIGH]` `drawerToggle` UninitializedPropertyAccessException — BaseActivity

**File**: `BaseActivity.kt` lines 35, 143

**Description**: Two `ActionBarDrawerToggle` fields are declared (`toggle` and `drawerToggle`), but only `toggle` is initialised in `initToolbarWithDrawer()`. `onOptionsItemSelected` calls `drawerToggle.onOptionsItemSelected(item)`, which will throw `UninitializedPropertyAccessException` on any menu item tap.

**Fix**: Remove the redundant `drawerToggle` field and replace its usage with `toggle`:
```kotlin
// Remove: private lateinit var drawerToggle: ActionBarDrawerToggle

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (toggle.onOptionsItemSelected(item)) {  // was: drawerToggle
        return true
    }
    return super.onOptionsItemSelected(item)
}
```

---

### BUG-05 `[HIGH]` String-Based Timestamp Comparison in FirebaseSyncManager

**File**: `FirebaseSyncManager.kt` lines 26, 33, 50, 57

**Description**: Timestamps are `Long` values (epoch milliseconds) but comparison is done as strings:
```kotlin
(local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()
```
String comparison on numbers is lexicographic: `"9000000000000" < "10000000000000"` (false numerically, true lexicographically because `"9" > "1"`). While all current epoch millisecond timestamps (13 digits) happen to sort correctly right now, this is semantically wrong, accidental, and will silently produce wrong results for any timestamp below ~`1000000000000` (Sept 2001).

**Fix**: Compare as Long directly:
```kotlin
// Replace:
(local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()
// With:
(local.dateJoined ?: 0L) > (remote.dateJoined ?: 0L)
```

---

### BUG-06 `[HIGH]` Follow-up Numbering Breaks After 9 Follow-ups — Web-App

**File**: `php/insertFollowUp.php`

**Description**: `follow_up_num` is calculated via:
```sql
SELECT MAX(follow_up_num) FROM follow_up_data WHERE id = '$id'
```
The column is `VARCHAR`, so `MAX()` uses lexicographic ordering: `MAX("1","2",...,"9","10")` returns `"9"` (not `"10"`), causing the 10th follow-up to be numbered `10` (correct by coincidence) but the 11th to be incorrectly numbered. With more entries, ordering breaks: `"9"` sorts after `"10"–"19"`, etc.

**Fix**: Either change `follow_up_num` to an `INT` column in the DB schema, or cast in the query:
```sql
SELECT MAX(CAST(follow_up_num AS UNSIGNED)) FROM follow_up_data WHERE id = ?
```

---

### BUG-07 `[MEDIUM]` Duplicate Navigation Handler in BaseActivity

**File**: `BaseActivity.kt` — `onDrawerItemSelected()` and `onNavigationItemSelected()`

**Description**: Both methods contain identical `when (item.itemId)` blocks navigating to the same destinations. The drawer listener calls `onDrawerItemSelected` but `BaseActivity` also implements `NavigationView.OnNavigationItemSelectedListener`. Both fire for the same event, potentially launching each destination Activity twice.

**Fix**: Remove `onDrawerItemSelected()` and keep only `onNavigationItemSelected()`, or remove the `setNavigationItemSelectedListener` lambda and route everything through the interface method.

---

### BUG-08 `[MEDIUM]` Follow-up Number Mismatch on Edit

**File**: `PatientFollowUpActivity.kt` line 157

**Description**: When adding a new follow-up, `currFollowUpNo = (totalInitialFollowUps + 1).toString()` uses the total count of existing follow-ups. If any follow-up has been deleted (once deletion is implemented), the count will produce a duplicate follow-up number. For example: if follow-ups 1, 2, 3 exist and follow-up 2 is deleted, the next add will generate number `3` again (count=2, +1=3).

**Fix**: Query `MAX(follow_up_num)` from the DAO instead of using count:
```kotlin
// Add to PatientFollowUpDao:
@Query("SELECT MAX(CAST(follow_up_num AS INT)) FROM follow_up_data WHERE id = :patientId")
suspend fun getMaxFollowUpNum(patientId: Int): Int?

// In collectPatientFollowUpFromInput():
currFollowUpNo = ((viewModel.getMaxFollowUpNum(patientId) ?: 0) + 1).toString()
```

---

### BUG-09 `[MEDIUM]` Orphaned Follow-up Records on Patient Delete — Web-App

**File**: `php/deleteRecord.php`

**Description**: Deleting a patient removes only the `patient_data` row. All associated `follow_up_data` rows referencing that `id` remain, accumulating as orphaned records with no parent patient.

**Fix**: Add a cascading delete:
```sql
DELETE FROM follow_up_data WHERE id = ?;
DELETE FROM patient_data WHERE id = ?;
```
Or enforce via a foreign key constraint with `ON DELETE CASCADE` in the schema.

---

### BUG-10 `[MEDIUM]` `SUM(paid)` on VARCHAR Silently Returns Wrong Results — Web-App

**File**: `includes/earning.php`

**Description**: The `paid` column in both `patient_data` and `follow_up_data` is `VARCHAR`. MySQL silently coerces non-numeric values to 0 in `SUM()`. If `paid` contains values like `"500 cash"`, `"N/A"`, or is empty, the earnings total will be silently incorrect with no error.

**Fix**: Change `paid` column to `DECIMAL(10,2)` in the schema, and enforce numeric input in the form and PHP endpoint. Apply the same fix to `balance` in `follow_up_data`.

---

### BUG-11 `[LOW]` Debug Toast in BaseActivity (Java Legacy)

**File**: `Mahajan Homeo Clinic/app/src/main/java/com/example/loginapp/BaseActivity.java` line 84

**Description**: `Toast.makeText(this, "BaseActivity Created", Toast.LENGTH_SHORT).show()` fires on every activity creation in the legacy app. This is a debug artefact visible to users.

**Fix**: Remove the toast.

---

### BUG-12 `[LOW]` `sampleData()` Test Method Left in FirebaseRepository

**File**: `FirebaseRepository.kt` lines 71–88

**Description**: A method `sampleData()` with hardcoded test patient data (id=132321, firstName="2", etc.) exists in the production `FirebaseRepository` class. If called accidentally, it writes garbage data to the live Firebase database.

**Fix**: Delete the method entirely. If test data is needed, use a test-specific file or mark it with `@VisibleForTesting`.

---

## Section 3: Architecture & Design Gaps

### ARCH-01 `[HIGH]` No Real Authentication in PatientRecords App

**Description**: The entire PatientRecords app is gated behind `LoginActivity` with hardcoded `a`/`a` credentials. Firebase Authentication is already a project dependency and is fully implemented in the sibling app. Without real auth, the app is unusable in production — anyone with the APK has full access to all patient data.

**Fix Plan**:
1. Add `com.google.firebase:firebase-auth` to `PatientRecords/app/build.gradle.kts`.
2. Refactor `LoginViewModel` to call `FirebaseAuth.signInWithEmailAndPassword()`.
3. Observe the result as a `LiveData<Result<FirebaseUser>>`.
4. Store auth state; auto-skip login if `FirebaseAuth.currentUser != null`.
5. Implement `nav_logout` in `BaseActivity` to call `FirebaseAuth.signOut()` and navigate to `LoginActivity` clearing the back stack.

---

### ARCH-02 `[HIGH]` No Logout Functionality

**Files**: `BaseActivity.kt` line 99 (Android), Web-App (no logout page)

**Description**: The logout menu item in the Android app has an empty handler. The web app has no logout link or `session_destroy()` call anywhere in the codebase.

**Fix (Android)**:
```kotlin
R.id.nav_logout -> {
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}
```

**Fix (Web-App)**: Create `logout.php`:
```php
<?php
session_start();
session_destroy();
header("Location: index.php");
exit();
```
Add "Logout" link to `includes/header.php` navigation.

---

### ARCH-03 `[MEDIUM]` No Foreign Key Enforcement in Room

**File**: `PatientFollowUp.kt`

**Description**: The `PatientFollowUp.id` field references `Patient.id` but there is no `@ForeignKey` annotation in the entity. Room will not enforce referential integrity — follow-up records can exist for deleted patients, and vice versa, without any error.

**Fix**: Add foreign key annotation to `PatientFollowUp`:
```kotlin
@Entity(
    tableName = "follow_up_data",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PatientFollowUp(...)
```
Also requires a Room database migration (`version = 2`).

---

### ARCH-04 `[MEDIUM]` No Database Migration Strategy

**File**: `PatientDatabase.kt`

**Description**: Room database is at version 1 with no `addMigrations()` defined. Any schema change (adding a column, changing a type, adding foreign keys) will require users to either provide a migration or accept `fallbackToDestructiveMigration()`, which deletes all data. There is currently no plan documented for migrations.

**Fix**:
1. Document current schema as the baseline version 1.
2. For each future schema change, write a `Migration(from, to)` object.
3. Add to `PatientDatabase.getInstance()` builder:
   ```kotlin
   Room.databaseBuilder(...)
       .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
       .build()
   ```

---

### ARCH-05 `[MEDIUM]` No Sync Between Web-App and Android App

**Description**: The web app operates on a MySQL database with no connection to the Firebase Realtime Database used by the Android app. Data entered in the web app is invisible to the Android app and vice versa. The system effectively has three separate, disconnected data stores.

**Fix Plan** (long-term):
- Option A: Build a REST API layer (PHP or separate service) that both apps talk to, backed by a single database.
- Option B: Migrate the web app to read/write Firebase Realtime DB (or Firestore) directly, eliminating MySQL.
- Option C (minimal): Add a one-way export from MySQL to Firebase as a PHP script triggered manually.

---

### ARCH-06 `[MEDIUM]` `launchWhenStarted` Deprecated Usage

**File**: `ViewAllPatientsActivity.kt`

**Description**: `lifecycleScope.launchWhenStarted` is deprecated in newer Lifecycle versions because it doesn't cancel the coroutine when the lifecycle moves below STARTED — it only suspends it, which can lead to resource leaks and unexpected behaviour on back-navigation.

**Fix**: Replace with `repeatOnLifecycle(Lifecycle.State.STARTED)`:
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.filteredPatients.collect { patients ->
            adapter.submitList(patients)
        }
    }
}
```

---

### ARCH-07 `[LOW]` `followUp1`–`followUp4` Denormalised Fields in Patient

**File**: `Patient.kt` lines 44–47

**Description**: The `Patient` entity has four `followUp1`–`followUp4` fields that appear to be legacy placeholders for follow-up dates. The actual follow-up data is fully normalised in the `follow_up_data` table. These fields are never populated in `AddPatientActivity`, never displayed in the UI, and never queried. They add noise to the data model and the Firebase sync payload.

**Fix**: Remove `followUp1`–`followUp4` from `Patient` and add a Room migration (version 1→2) to drop those columns.

---

### ARCH-08 `[LOW]` No ProGuard/R8 on Release Builds

**File**: `PatientRecords/app/build.gradle.kts` line 25

**Description**: `isMinifyEnabled = false` means release APKs contain unobfuscated class names, method names, and string literals. Firebase configuration, API keys referenced in code, and application logic are fully readable via decompilation.

**Fix**: Enable minification and add appropriate ProGuard rules:
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
Add keep rules for Room entities and Firebase data classes.

---

### ARCH-09 `[LOW]` Zero Test Coverage

**Description**: Both Android apps contain only auto-generated placeholder tests. No unit tests for ViewModels, no DAO integration tests (Room in-memory), no UI tests (Espresso), and no PHP unit tests for the web app. This makes refactoring and bug fixing high-risk.

**Recommended Test Plan**:
1. **DAO tests**: Use Room's in-memory database in androidTest to test all queries including the dashboard INNER JOIN and the search query.
2. **ViewModel tests**: Unit test `FirebaseSyncManager` logic using mock repositories to verify the comparison and merge logic.
3. **UI smoke tests**: Espresso tests for the add-patient and add-follow-up flows to catch crashes like BUG-01.
4. **Web-App**: PHPUnit tests for the SQL-building logic in `filter.php` and `earning.php`.

---

## Prioritised Fix Roadmap

### Phase 1 — Critical Security (fix before any production deployment)
1. **SEC-01**: Convert all Web-App SQL to prepared statements.
2. **SEC-02**: Hash admin passwords in Web-App.
3. **SEC-03**: Remove plaintext password from Firebase in legacy app.
4. **SEC-04**: Escape all HTML output in Web-App with `htmlspecialchars`.
5. **SEC-05**: Replace hardcoded login with Firebase Authentication in PatientRecords.

### Phase 2 — High-Impact Bugs
6. **BUG-01**: Fix `toInt()` crash in `PatientFollowUpActivity`.
7. **BUG-02**: Remove `Random.nextInt()` ID generation; rely on Room autoGenerate.
8. **BUG-03**: Preserve `dateJoined` on patient update.
9. **BUG-04**: Fix uninitialized `drawerToggle` crash in `BaseActivity`.
10. **BUG-05**: Fix string-based timestamp comparison in `FirebaseSyncManager`.
11. **ARCH-02**: Implement logout in both Android app and Web-App.

### Phase 3 — Data Integrity
12. **BUG-06**: Fix follow-up number ordering for VARCHAR column in Web-App.
13. **BUG-09**: Cascade delete follow-ups when patient is deleted in Web-App.
14. **BUG-10**: Change `paid`/`balance` to numeric columns in MySQL.
15. **ARCH-03**: Add `@ForeignKey` constraint in Room with Room migration v1→v2.
16. **ARCH-07**: Remove `followUp1`–`followUp4` fields from `Patient` entity (same migration).

### Phase 4 — Architecture Improvements
17. **ARCH-05**: Design and implement data synchronisation between Web-App and Android.
18. **ARCH-04**: Document and implement Room migration strategy.
19. **SEC-06**: Add server-side input validation to all PHP form handlers.
20. **SEC-07**: Move `session_start()` to top of `logincheck.php`.
21. **SEC-08**: Add CSRF tokens to Web-App forms.
22. **ARCH-06**: Migrate `launchWhenStarted` to `repeatOnLifecycle`.
23. **BUG-07**: Deduplicate navigation handler in `BaseActivity`.
24. **BUG-08**: Fix follow-up number generation to use MAX instead of COUNT.

### Phase 5 — Quality & Maintenance
25. **ARCH-08**: Enable ProGuard/R8 for release builds.
26. **ARCH-09**: Write DAO tests, ViewModel unit tests, and UI smoke tests.
27. **BUG-11**: Remove debug toast from legacy `BaseActivity`.
28. **BUG-12**: Remove `sampleData()` test method from `FirebaseRepository`.
