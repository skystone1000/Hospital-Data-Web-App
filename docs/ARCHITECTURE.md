# Architecture — Hospital Data Web App

## 1. System Overview

The Hospital Data Web App is a multi-platform clinic management system tailored for homeopathic case-taking and patient record management. It consists of three independent but conceptually related sub-systems:

| Sub-system | Technology | Status |
|---|---|---|
| **PatientRecords** | Android (Kotlin, MVVM, Room, Firebase) | Active — primary app |
| **Mahajan Homeo Clinic** | Android (Java, Firebase Auth) | Legacy — authentication prototype |
| **Web-App** | PHP + MySQL + Bootstrap | Active — desktop admin interface |

These three sub-systems do **not share a runtime**: they operate against separate database instances (Room DB on device, Firebase Realtime DB in cloud, MySQL on server) with no live synchronisation between the Android app and the web app.

---

## 2. Sub-system: PatientRecords (Android — Kotlin)

### 2.1 Layer Diagram

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer                          │
│  Activities → ViewModels (LiveData / StateFlow)     │
│  BaseActivity (Drawer + Toolbar)                    │
└──────────────────────┬──────────────────────────────┘
                       │  ViewModel calls
┌──────────────────────▼──────────────────────────────┐
│                Repository Layer                     │
│  PatientRepository  ←→  FirebaseSyncManager         │
└──────┬───────────────────────────────┬──────────────┘
       │                               │
┌──────▼──────┐                ┌───────▼──────────────┐
│  Local DB   │                │  Remote DB           │
│  Room DB    │                │  Firebase Realtime   │
│  (SQLite)   │                │  Database            │
└─────────────┘                └──────────────────────┘
```

### 2.2 Architecture Pattern

- **MVVM** (Model-View-ViewModel) with Repository pattern.
- **Data Binding** used in all Activities for two-way UI binding.
- **ViewModelFactory** pattern used to inject `PatientRepository` into each ViewModel.
- **Application class** (`PatientRecordsApp`) acts as a manual DI container, initialising `PatientRepository` and `FirebaseRepository` once at startup.

### 2.3 Data Layer

#### Room Database (`PatientDatabase.kt`)
- Version: 1
- Entities: `Patient` (`patient_data` table), `PatientFollowUp` (`follow_up_data` table)
- Singleton via `getInstance(context)`
- DAOs: `PatientDao`, `PatientFollowUpDao`

#### Patient Entity (`patient_data`)
```
id              INT  PK AUTOINCREMENT
firstName       TEXT NOT NULL
middleName      TEXT NULLABLE
lastName        TEXT NOT NULL
age             INT
sex             TEXT NULLABLE
occupation      TEXT NULLABLE
address         TEXT NULLABLE
phone           TEXT NULLABLE
regno           TEXT NULLABLE
height          INT  NULLABLE
weight          INT  NULLABLE
cc1, cc2, cc3   TEXT NULLABLE    -- Chief Complaints
appetite        TEXT NULLABLE    -- Homeopathic: appetite
desire          TEXT NULLABLE
aversions       TEXT NULLABLE
thirst          TEXT NULLABLE
perspiration    TEXT NULLABLE
sleep           TEXT NULLABLE
stool           TEXT NULLABLE
urine           TEXT NULLABLE
menses          TEXT NULLABLE
thermal         TEXT NULLABLE
mind            TEXT NULLABLE
hobbies         TEXT NULLABLE
particulars     TEXT NULLABLE
on_examination  TEXT NULLABLE
path_inv        TEXT NULLABLE
previous_rx     TEXT NULLABLE
past_history    TEXT NULLABLE
family_history  TEXT NULLABLE
treatment       TEXT NULLABLE
paid            TEXT NULLABLE
balance         TEXT NULLABLE
followUp1..4    TEXT NULLABLE    -- Denormalised follow-up references (legacy)
dateJoined      LONG (epoch ms)
urlToImage      TEXT NULLABLE
```

#### PatientFollowUp Entity (`follow_up_data`)
```
followUpId        INT  PK AUTOINCREMENT
id                INT  (FK → patient_data.id, NOT enforced by Room)
date              LONG (epoch ms)
regno             TEXT
follow_up_num     TEXT
weight            INT
treatment_output  TEXT
other_complains   TEXT
treatment         TEXT
medicine_duration TEXT
paid              TEXT
balance           INT
```

#### Firebase Realtime Database
- Node `patients/` — mirrors the `patient_data` structure.
- Node `patient_follow_ups/` — mirrors `follow_up_data` structure.
- Keyed by `id` and `followUpId` respectively.

### 2.4 Repository Layer

| Class | Responsibility |
|---|---|
| `PatientRepository` | CRUD + search + dashboard queries against Room DB |
| `FirebaseRepository` | Upload/download lists to/from Firebase Realtime DB |
| `FirebaseSyncManager` | Bidirectional merge logic; compares local vs remote, pushes delta each way |

### 2.5 UI Layer

#### Navigation Flow
```
LoginActivity
    └── MainActivity (Home)
            ├── ViewAllPatientsActivity  ──►  PatientHistoryActivity
            │       (RecyclerView + Search)         ├── PatientFollowUpActivity (Add/View/Edit)
            │                                       └── AddPatientActivity (View/Edit)
            ├── AddPatientActivity (Add new)
            ├── DashboardActivity
            └── BackUpActivity
```

All activities except `LoginActivity` extend `BaseActivity`, which provides:
- Navigation drawer (50% screen width) with links to all top-level screens.
- `MaterialToolbar` with white hamburger icon.
- Edge-to-edge status bar inset handling.

#### Activity × ViewModel Map
| Activity | ViewModel | Key LiveData/StateFlow |
|---|---|---|
| `AddPatientActivity` | `AddPatientViewModel` | `patientLiveData` |
| `ViewAllPatientsActivity` | `ViewAllPatientsViewModel` | `filteredPatients` (StateFlow) |
| `PatientHistoryActivity` | `PatientHistoryViewModel` | `patient`, `followUps` (StateFlow) |
| `PatientFollowUpActivity` | `PatientFollowUpViewModel` | `patient`, `patientFollowUps` |
| `DashboardActivity` | `DashboardViewModel` | 8× LiveData counts + 2× StateFlow lists |
| `BackUpActivity` | `BackUpViewModel` | — (fire-and-forget coroutines) |

---

## 3. Sub-system: Mahajan Homeo Clinic (Android — Java Legacy)

### 3.1 Purpose
An earlier prototype that implemented the **authentication and registration** flow with Firebase Auth and Firebase Realtime Database. The patient data management features were never ported here.

### 3.2 Component Diagram
```
MainActivity (Login)
    ├── RegisterActivity
    │       └── VerifyActivity (Phone OTP via Firebase)
    └── DashboardActivity (stub)
```

### 3.3 Key Classes
| Class | Role |
|---|---|
| `MainActivity.java` | Email/password login via Firebase Auth; AuthStateListener for auto-login |
| `RegisterActivity.java` | Registration form; creates Firebase Auth user + stores record in `Users` node |
| `VerifyActivity.java` | Phone OTP verification; 60s timeout; hardcoded country code +91 |
| `User.java` | Data model for Firebase DB (Displayname, Email, createdAt) |
| `BaseActivity.java` | Abstract activity; toolbar + logout; tracks current activity via `Clinic` app class |
| `Clinic.java` | Application subclass tracking the current foreground activity |
| `ProductGridFragment.java` | Unused scaffold fragment from Material Design template |

---

## 4. Sub-system: Web-App (PHP + MySQL)

### 4.1 Technology Stack
- **Backend**: PHP (no framework), MySQLi extension
- **Frontend**: Bootstrap 4.3.1, Material Design Bootstrap, jQuery 3.4.1
- **Voice input**: Annyang + SpeechKITT (experimental)
- **Database**: MySQL, database name `hospital`

### 4.2 Page Flow
```
index.php (Login)
    └── home.php (Welcome / Hub)
            ├── records.php (Patient list + pagination)
            │       └── patientDetails.php
            │               ├── followUp.php (Add follow-up)
            │               └── patientDetailsEdit.php (Edit patient)
            ├── fillForm.php (Add new patient)
            ├── dashboard.php (Statistics + summary)
            ├── detailedRecords.php
            └── search.php
```

### 4.3 Database Schema (MySQL)

#### `admin_users`
```sql
id_admin       INT  PK AUTO_INCREMENT
uid_admin      TINYTEXT
email_admin    TINYTEXT
password_admin LONGTEXT             -- plaintext (critical issue)
dateOfBirth    DATE/DATETIME
degree         TEXT
firstName      TEXT
lastName       TEXT
```

#### `patient_data`
```sql
id             INT  PK AUTO_INCREMENT
firstName      VARCHAR
middleName     VARCHAR
lastName       VARCHAR
age            INT
sex            VARCHAR
occupation     VARCHAR
address        TEXT
phone          VARCHAR
regno          VARCHAR
height         INT
weight         INT
diagnosis      VARCHAR
cc1, cc2, cc3  TEXT                 -- Chief Complaints
appetite, desire, aversions, thirst, perspiration,
sleep, stool, urine, menses, thermal, mind   TEXT
hobbies, particulars, on_examination, path_inv,
previous_rx, past_history, family_history    TEXT
treatment      TEXT
paid           VARCHAR
balance        VARCHAR
followUp1..4   VARCHAR
dateJoined     DATETIME  DEFAULT CURRENT_TIMESTAMP
```

#### `follow_up_data`
```sql
followUpId          INT  PK AUTO_INCREMENT
id                  INT  FK → patient_data.id (not enforced at DB level)
date                DATETIME
regno               VARCHAR
weight              VARCHAR
treatment_output    VARCHAR
other_complains     TEXT
treatment           TEXT
medicine_duration   VARCHAR
paid                VARCHAR
balance             VARCHAR
```

### 4.4 PHP File Responsibilities

| File | Role |
|---|---|
| `index.php` | Login form UI |
| `includes/logincheck.php` | POST handler; session setup |
| `includes/connection.php` | MySQLi connection singleton |
| `includes/header.php` | Session guard; Bootstrap navbar include |
| `home.php` | Landing page with navigation cards |
| `fillForm.php` | Add-patient form with voice input |
| `php/insertRecord.php` | INSERT patient via GET params |
| `php/insertFollowUp.php` | INSERT follow-up; auto-calculates follow_up_num |
| `php/updateRecord.php` | UPDATE patient via GET params |
| `php/deleteRecord.php` | DELETE patient (no cascade) |
| `records.php` | Paginated patient list |
| `includes/filter.php` | Sort/filter SQL builder + pagination |
| `patientDetails.php` | Patient + follow-ups view |
| `patientDetailsEdit.php` | Edit patient form |
| `followUp.php` | Add follow-up form |
| `dashboard.php` | Statistics page |
| `includes/earning.php` | Aggregated counts and earnings SQL |
| `search.php` | Search results |
| `updateRecord.php` | Edit patient entry point |
| `js/script.js` | Annyang voice command wiring |

---

## 5. Cross-System Data Model Mapping

| Concept | PatientRecords (Room) | Web-App (MySQL) | Firebase |
|---|---|---|---|
| Patient | `patient_data` entity | `patient_data` table | `patients/` node |
| Follow-up | `follow_up_data` entity | `follow_up_data` table | `patient_follow_ups/` node |
| Patient PK | `id` (INT, auto) | `id` (INT, auto) | string key from `id` |
| Follow-up PK | `followUpId` (INT, auto) | `followUpId` (INT, auto) | string key from `followUpId` |
| Timestamp (patient) | `dateJoined` (Long ms) | `dateJoined` (DATETIME) | `dateJoined` (Long ms) |
| Timestamp (follow-up) | `date` (Long ms) | `date` (DATETIME) | `date` (Long ms) |

No automated sync exists between the MySQL web database and the Firebase / Room databases.

---

## 6. Key Architectural Decisions

| Decision | Rationale | Trade-off |
|---|---|---|
| Manual DI via Application class | Avoids Hilt/Dagger complexity | No scoping; all repos are app-scoped singletons |
| ViewModelFactory for each ViewModel | Allows constructor injection of repository | Boilerplate per ViewModel |
| Room + Firebase dual storage | Offline-first with cloud backup | Sync complexity; currently only manually triggered |
| Long (epoch ms) for dates in Android | Enables numeric SQL range queries | Mismatch with web app's DATETIME format |
| PHP without a framework | Rapid prototyping | No routing, no MVC, significant security surface |
| Data Binding + MVVM | Reduces boilerplate UI code | Requires careful null handling; increases compile time |
