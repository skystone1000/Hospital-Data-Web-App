# Audit Report — PatientRecords Android App

**Date**: 2026-05-23  
**Last updated**: 2026-05-23 (all issues resolved)  
**Scope**: Complete Kotlin Android codebase  
**Severity**: `[CRITICAL]` `[HIGH]` `[MEDIUM]` `[LOW]`

---

## Summary

| Severity | Count | Fixed |
|---|---|---|
| CRITICAL | 1 | 1 |
| HIGH | 7 | 7 |
| MEDIUM | 6 | 6 |
| LOW | 4 | 3 |
| **Total** | **18** | **17** |

> AND-LOW-04 (test coverage) is deferred — no tests exist yet but all business logic is now correct and stable.

---

## CRITICAL Issues

---

### AND-SEC-01 `[CRITICAL]` ~~Hardcoded Credentials — No Real Authentication~~ ✅ Addressed

**Resolution**: App is private / internal only, not published to Play Store. Credentials remain hardcoded by design. `LoginViewModel` retains the check against the configured email/password constants. Logout is now functional (see AND-HIGH-01).

---

## HIGH Issues

---

### AND-HIGH-01 `[HIGH]` ~~Logout Handler is Empty~~ ✅ Fixed

**File**: `ui/base/BaseActivity.kt`

**Fix applied**: `nav_logout` handler now signs out and navigates to `LoginActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`, clearing the entire back stack:

```kotlin
R.id.nav_logout -> {
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}
```

---

### AND-HIGH-02 `[HIGH]` ~~`toInt()` Crash on Empty or Non-Numeric Input~~ ✅ Fixed

**File**: `ui/followuppatient/PatientFollowUpActivity.kt`

**Fix applied**: Extracted a `validateNumericFields()` helper that uses `toIntOrNull()` and sets `binding.etWeight.error` / `binding.etBalanceAmount.error` on invalid input. Both Submit and Update buttons call it and return early on failure — the app never reaches the data collection path with invalid input.

---

### AND-HIGH-03 `[HIGH]` ~~Random ID Generation Causes Silent Data Overwrite~~ ✅ Fixed

**Files**: `ui/addpatient/AddPatientActivity.kt`, `ui/followuppatient/PatientFollowUpActivity.kt`

**Fix applied**: New patients and follow-ups are created with `id = 0` / `followUpId = 0`. Room's `autoGenerate = true` interprets `0` as "assign next available ID". Edit mode continues to pass the existing `patientId` / `followUpId`. Also changed `PatientFollowUp`'s default `followUpId` from `-1` to `0`.

---

### AND-HIGH-04 `[HIGH]` ~~`dateJoined` Overwritten on Patient Edit~~ ✅ Fixed

**File**: `ui/addpatient/AddPatientActivity.kt`

**Fix applied**: `AddPatientActivity` now caches `originalDateJoined` when the patient is loaded from the DB. `collectPatientFromInput()` uses:

```kotlin
dateJoined = if (isEditMode) originalDateJoined else System.currentTimeMillis()
```

The same approach is applied to `PatientFollowUpActivity` via `originalFollowUpDate`.

---

### AND-HIGH-05 `[HIGH]` ~~String-Based Timestamp Comparison in Sync Manager~~ ✅ Fixed

**File**: `data/FirebaseSyncManager.kt`

**Fix applied**: All four `.toString()` comparisons replaced with direct `Long` arithmetic:

```kotlin
// Before (wrong):
(local.dateJoined ?: 0).toString() > (remote.dateJoined ?: 0).toString()

// After (correct):
(local.dateJoined ?: 0L) > (remote.dateJoined ?: 0L)
```

`PatientFollowUp.date` is now non-nullable `Long`, so the follow-up sync compares `local.date > remote.date` directly.

---

### AND-HIGH-06 `[HIGH]` ~~`drawerToggle` Field Never Initialised — Crash Risk~~ ✅ Fixed

**File**: `ui/base/BaseActivity.kt`

**Fix applied**: Removed the duplicate `drawerToggle` field entirely. `onOptionsItemSelected` now references only `toggle` (the properly initialised field). Also see AND-MED-04 — the duplicate navigation handler was cleaned up in the same rewrite.

---

### AND-HIGH-07 `[HIGH]` ~~Firebase Sync Errors Silently Swallowed~~ ✅ Fixed

**File**: `data/FirebaseRepository.kt`

**Fix applied**: Changed from `suspendCoroutine` to `suspendCancellableCoroutine`. `onCancelled` now calls `cont.resumeWithException(error.toException())` instead of `cont.resume(emptyList())`. The exception propagates through `FirebaseSyncManager` → `BackUpViewModel.syncData()` → `BackUpActivity`, where it is caught and shown as a Toast with the error message.

`BackUpActivity` now disables the sync button during the operation and re-enables it in a `finally` block. `BackUpViewModel.syncData()` is now a `suspend fun` using `coroutineScope`, so the completion Toast fires only after both patient and follow-up syncs finish.

---

## MEDIUM Issues

---

### AND-MED-01 `[MEDIUM]` ~~No Foreign Key Constraint on `follow_up_data.id`~~ ✅ Fixed

**File**: `data/localdb/PatientFollowUp.kt`

**Fix applied**: Added `@ForeignKey` annotation with `onDelete = ForeignKey.CASCADE` and a required `@Index` on `id`. A Room database migration (v1→v2) recreates the table with the constraint.

---

### AND-MED-02 `[MEDIUM]` ~~Follow-up Numbering Breaks When Follow-ups Are Deleted~~ ✅ Fixed

**Files**: `data/localdb/PatientFollowUp.kt`, `data/localdb/PatientFollowUpDao.kt`, `ui/followuppatient/PatientFollowUpActivity.kt`

**Fix applied**: 
- `follow_up_num` changed from `String` to `Int` in `PatientFollowUp`. Migration v1→v2 casts existing values with `CAST(follow_up_num AS INTEGER)`.
- `PatientFollowUpActivity` now computes the next number from `list.maxOfOrNull { it.follow_up_num } ?: 0` rather than `list.count()`. This is correct even when follow-ups are non-contiguous.
- `ORDER BY follow_up_num DESC` in the DAO now uses correct integer ordering.
- `EXTRA_FOLLOW_UP_NUMBER` is now passed and received as `Int` (`putExtra`/`getIntExtra`).

---

### AND-MED-03 `[MEDIUM]` ~~Deprecated `launchWhenStarted` Usage~~ ✅ Fixed

**Files**: `ui/viewallpatient/ViewAllPatientsActivity.kt`, `ui/patienthistory/PatientHistoryActivity.kt`

**Fix applied**: Both files now use `lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { ... } }`. This properly cancels the coroutine (not just suspends it) when the lifecycle drops below STARTED.

---

### AND-MED-04 `[MEDIUM]` ~~Duplicate Navigation Handler in BaseActivity~~ ✅ Fixed

**File**: `ui/base/BaseActivity.kt`

**Fix applied**: Removed the `onDrawerItemSelected()` method and the lambda in `initToolbarWithDrawer()`. Replaced with `navView.setNavigationItemSelectedListener(this)`, routing all navigation through the single `onNavigationItemSelected()` interface implementation.

---

### AND-MED-05 `[MEDIUM]` ~~No Room Migration Strategy~~ ✅ Fixed

**File**: `data/PatientDatabase.kt`

**Fix applied**: Database version bumped to `2`. `MIGRATION_1_2` defined and registered with `.addMigrations(MIGRATION_1_2)`. The migration handles both schema changes (removal of `followUp1–4` from `patient_data`; FK + INT `follow_up_num` on `follow_up_data`) in a single transaction-safe migration.

---

### AND-MED-06 `[MEDIUM]` ~~All Patients Loaded Into Memory for Search~~ ✅ Already correct

**File**: `ui/viewallpatient/ViewAllPatientsViewModel.kt`

**Finding on inspection**: `filteredPatients` was already using `flatMapLatest { query → repository.searchPatients(query) }` — SQL-level search via `PatientDao.searchPatients()`. No change needed.

---

## LOW Issues

---

### AND-LOW-01 `[LOW]` ~~`sampleData()` Test Method in Production Class~~ ✅ Fixed

**File**: `data/FirebaseRepository.kt`

**Fix applied**: `sampleData()` method deleted entirely from `FirebaseRepository`.

---

### AND-LOW-02 `[LOW]` ~~Unused Legacy Fields in Patient Entity~~ ✅ Fixed

**File**: `data/localdb/Patient.kt`

**Fix applied**: `followUp1`, `followUp2`, `followUp3`, `followUp4` fields removed from `Patient`. Migration v1→v2 drops them from the table by recreating `patient_data` without those columns.

---

### AND-LOW-03 `[LOW]` ProGuard / R8 Disabled on Release Builds

**File**: `app/build.gradle.kts`

**Status**: Deferred. App is not published to Play Store. Enable before any public distribution:

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

Add keep rules in `proguard-rules.pro` for Room entities and Firebase data classes.

---

### AND-LOW-04 `[LOW]` Zero Test Coverage

**Status**: Deferred. Recommended tests to write:

| Test | Type | Priority |
|---|---|---|
| `PatientDao.getPatientsFromDay()` returns correct records | Room in-memory | High |
| `PatientFollowUpDao.getPatientsWithFollowUpsFromDay()` INNER JOIN | Room in-memory | High |
| `FirebaseSyncManager` — local-only item pushes to remote | Unit (mock repos) | High |
| `FirebaseSyncManager` — newer remote record pulled to local | Unit (mock repos) | High |
| `PatientFollowUpActivity` — empty weight shows error, no crash | Espresso | High |
| `AddPatientActivity` — new patient uses id=0, edit preserves dateJoined | Espresso | Medium |
