# Architecture — PatientRecords Android App

## 1. Overview

PatientRecords is an offline-first Android application for homeopathic patient case management. It stores patient records and follow-ups locally in a Room (SQLite) database and supports manual cloud sync to Firebase Realtime Database.

| Property | Value |
|---|---|
| Language | Kotlin |
| Architecture pattern | MVVM + Repository |
| Local DB | Room 2.6.1 (SQLite) |
| Remote DB | Firebase Realtime Database |
| UI binding | Data Binding + View Binding |
| Async | Kotlin Coroutines + Flow |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## 2. Layer Diagram

```
┌───────────────────────────────────────────────────────┐
│                      UI Layer                         │
│                                                       │
│  LoginActivity                                        │
│  MainActivity                                         │
│  AddPatientActivity  ──►  AddPatientViewModel         │
│  ViewAllPatientsActivity ──►  ViewAllPatientsViewModel│
│  PatientHistoryActivity ──►  PatientHistoryViewModel  │
│  PatientFollowUpActivity ──►  PatientFollowUpViewModel│
│  DashboardActivity ──►  DashboardViewModel            │
│  BackUpActivity ──►  BackUpViewModel                  │
│                                                       │
│  BaseActivity (Drawer + Toolbar — shared base)        │
└────────────────────────┬──────────────────────────────┘
                         │  ViewModels call
┌────────────────────────▼──────────────────────────────┐
│                  Repository Layer                     │
│                                                       │
│  PatientRepository         FirebaseSyncManager        │
│  (Room DAO wrapper)    ◄──►  (bidirectional merge)    │
│                                    │                  │
│                              FirebaseRepository       │
│                              (Firebase R/W)           │
└────────┬──────────────────────────┬───────────────────┘
         │                          │
┌────────▼────────┐      ┌──────────▼──────────────┐
│   Local DB      │      │    Remote DB             │
│   Room / SQLite │      │  Firebase Realtime DB    │
│  patient_data   │      │  /patients               │
│  follow_up_data │      │  /patient_follow_ups     │
└─────────────────┘      └──────────────────────────┘
```

---

## 3. Application Class — Dependency Container

`PatientRecordsApp` (extends `Application`) acts as a manual DI container. It initialises both repositories once at app startup and exposes them as public properties:

```kotlin
class PatientRecordsApp : Application() {
    lateinit var repository: PatientRepository
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate() {
        val db = PatientDatabase.getInstance(this)
        repository = PatientRepository(db.patientDao(), db.patientFollowUpDao())
        firebaseRepository = FirebaseRepository()
    }
}
```

All Activities access repositories via `(application as PatientRecordsApp).repository`.

---

## 4. Data Layer

### 4.1 Room Database

**`PatientDatabase`** — Room singleton, version 1.

| Entity | Table | Primary Key | Notes |
|---|---|---|---|
| `Patient` | `patient_data` | `id` (autoGenerate) | 49 fields; `dateJoined` as Long (epoch ms) |
| `PatientFollowUp` | `follow_up_data` | `followUpId` (autoGenerate) | FK to `Patient.id` — not enforced |

**`PatientDao`** — CRUD + search + dashboard queries for patients.  
**`PatientFollowUpDao`** — CRUD + dashboard queries for follow-ups; includes an INNER JOIN query returning `Patient` rows that had a follow-up in a given period.

### 4.2 Repository

**`PatientRepository`** — thin wrapper around both DAOs. Translates between coroutine suspend functions and Flow for UI consumers.

### 4.3 Firebase Layer

**`FirebaseRepository`** — reads/writes to Firebase Realtime Database.
- Writes: per-child `setValue()` — no batch, no transaction.
- Reads: `addListenerForSingleValueEvent` wrapped in `suspendCoroutine`.
- Cancellation: resumes with `emptyList()` on error — no error propagation to callers.

**`FirebaseSyncManager`** — bidirectional merge logic invoked manually from `BackUpActivity`.

```
syncPatientsBothWays():
  local  = Room.getAllPatients().first()
  remote = Firebase.downloadPatients()
  toFirebase = local items where remote is absent OR local.dateJoined > remote.dateJoined
  toRoom     = remote items where local is absent OR remote.dateJoined > local.dateJoined
  Firebase.upload(toFirebase)
  Room.insert(toRoom)  ← uses REPLACE strategy
```

The same pattern is applied for `syncPatientFollowUpsBothWays()`.

---

## 5. UI Layer

### 5.1 Navigation Flow

```
LoginActivity
    │
    └──► MainActivity (Home)
             ├──► AddPatientActivity (Add mode)
             ├──► ViewAllPatientsActivity
             │        └──► PatientHistoryActivity
             │                   ├──► PatientFollowUpActivity (View/Edit mode)
             │                   └──► AddPatientActivity (View/Edit mode)
             ├──► DashboardActivity
             └──► BackUpActivity
```

Navigation is driven by explicit `Intent` + `startActivity`. There is no Jetpack Navigation Component or back-stack management beyond the default Android back stack.

### 5.2 BaseActivity

All post-login activities extend `BaseActivity`, which provides:
- `DrawerLayout` containing `MaterialToolbar` + `NavigationView`.
- `setChildContentView(view)` to inject each activity's content into a `FrameLayout` container.
- `initToolbarWithDrawer()` — sets up the hamburger toggle, white icon tint, 50% screen width drawer.
- `applyTopPaddingToRoot()` — edge-to-edge status bar inset handling.
- Navigation drawer menu items: Home, Add Patient, View Patients, Dashboard, Backup, Logout.

### 5.3 ViewModel Pattern

Each screen has a dedicated ViewModel created via a `ViewModelFactory` that accepts `PatientRepository` (and optionally `FirebaseRepository` or a `patientId`) as constructor parameters.

| ViewModel | Key state | Async mechanism |
|---|---|---|
| `AddPatientViewModel` | `patientLiveData: LiveData<Patient>` | `viewModelScope.launch` |
| `ViewAllPatientsViewModel` | `filteredPatients: StateFlow<List<Patient>>` | Flow + `debounce(300)` |
| `PatientHistoryViewModel` | `patient: LiveData`, `followUps: StateFlow` | `viewModelScope.launch` |
| `PatientFollowUpViewModel` | `patient: LiveData`, `patientFollowUps: LiveData` | `viewModelScope.launch` |
| `DashboardViewModel` | 8× `LiveData<Int>`, 2× `StateFlow<List<Patient>>` | `viewModelScope.launch` |
| `BackUpViewModel` | none (fire-and-forget) | `viewModelScope.launch` |

---

## 6. Database Schema

### `patient_data`
```
id              INTEGER  PRIMARY KEY AUTOINCREMENT
firstName       TEXT     NOT NULL
middleName      TEXT
lastName        TEXT     NOT NULL
age             INTEGER
sex             TEXT
occupation      TEXT
address         TEXT
phone           TEXT
regno           TEXT
height          INTEGER
weight          INTEGER
cc1, cc2, cc3   TEXT                   -- Chief Complaints
appetite        TEXT
desire          TEXT
aversions       TEXT
thirst          TEXT
perspiration    TEXT
sleep           TEXT
stool           TEXT
urine           TEXT
menses          TEXT
thermal         TEXT
mind            TEXT
hobbies         TEXT
particulars     TEXT
on_examination  TEXT
path_inv        TEXT
previous_rx     TEXT
past_history    TEXT
family_history  TEXT
treatment       TEXT
paid            TEXT
balance         TEXT
followUp1       TEXT                   -- legacy; never populated in UI
followUp2       TEXT
followUp3       TEXT
followUp4       TEXT
dateJoined      INTEGER                -- epoch milliseconds
urlToImage      TEXT
```

### `follow_up_data`
```
followUpId        INTEGER  PRIMARY KEY AUTOINCREMENT
id                INTEGER              -- references patient_data.id (not enforced)
date              INTEGER              -- epoch milliseconds
regno             TEXT
follow_up_num     TEXT
weight            INTEGER
treatment_output  TEXT
other_complains   TEXT
treatment         TEXT
medicine_duration TEXT
paid              TEXT
balance           INTEGER
```

---

## 7. Firebase Realtime Database Structure

```
Firebase Realtime Database
├── patients/
│   ├── <patient.id>/          ← Patient object (all fields)
│   └── ...
└── patient_follow_ups/
    ├── <patientFollowUp.followUpId>/   ← PatientFollowUp object (all fields)
    └── ...
```

---

## 8. Key Architectural Decisions

| Decision | Rationale | Trade-off |
|---|---|---|
| Manual DI via Application class | No Hilt/Dagger complexity | All repos are app-scoped singletons; no scoped lifetimes |
| ViewModelFactory per ViewModel | Constructor injection of repository | One factory class per ViewModel — boilerplate |
| Room + Firebase dual storage | Offline-first with optional cloud backup | Sync logic complexity; currently manual only |
| `Long` epoch ms for all timestamps | Enables numeric SQL range comparisons | Mismatch with web app's MySQL `DATETIME` format |
| `OnConflictStrategy.REPLACE` on insert | Handles upsert for sync | Silently overwrites records with matching `id` — collision risk with Random.nextInt() IDs |
| Data Binding + MVVM | Reduces UI boilerplate | Increases build time; requires careful null handling |
| No Jetpack Navigation | Simpler for small app | Back stack not managed; Activities can accumulate |

---

## 9. Build Configuration

| Property | Value |
|---|---|
| `compileSdk` | 35 |
| `minSdk` | 24 |
| `targetSdk` | 35 |
| `versionCode` | 1 |
| `versionName` | 1.0 |
| ViewBinding | enabled |
| DataBinding | enabled |
| ProGuard (release) | **disabled** (`isMinifyEnabled = false`) |
| Room annotation processor | KSP |
| Firebase | via `google-services` plugin |
