# Features — PatientRecords Android App

Status legend:  
`[x]` Implemented  `[~]` Partially implemented  `[ ]` Planned / not started

---

## Authentication & Session

| Feature | Status | Notes |
|---|---|---|
| Login screen | `[x]` | UI complete; hardcoded `a` / `a` credentials — not production-safe |
| Session persistence | `[ ]` | No SharedPreferences or Firebase token; re-login required every launch |
| Firebase Authentication | `[ ]` | Dependency not added; `LoginViewModel` uses hardcoded check |
| Logout | `[~]` | Drawer menu item exists; `nav_logout` handler body is empty — does nothing |
| Auto-login on app restart | `[ ]` | Not implemented |
| Biometric / PIN login | `[ ]` | Not implemented |

---

## Patient Management

| Feature | Status | Notes |
|---|---|---|
| Add new patient | `[x]` | 35+ fields covering full homeopathic case history |
| View patient list | `[x]` | RecyclerView; displays name, sex, occupation, phone, regno |
| Search patients | `[x]` | Debounced 300ms; searches firstName, middleName, lastName |
| View patient details | `[x]` | Read-only mode in `AddPatientActivity` |
| Edit patient details | `[x]` | Edit mode unlocks all fields; Update persists changes |
| Delete patient | `[ ]` | `PatientDao.delete()` and `PatientRepository.deletePatient()` exist; no UI entry point |
| Patient photo | `[~]` | `urlToImage` field in data model; no camera or gallery UI |
| Pagination / lazy loading | `[ ]` | All patients loaded into memory at once |
| Advanced filter (sex, date range, etc.) | `[ ]` | Not implemented |
| Export patient data | `[ ]` | Not implemented |

---

## Follow-up Management

| Feature | Status | Notes |
|---|---|---|
| Add follow-up | `[x]` | Weight, treatment output, complaints, treatment, medicine duration, paid, balance |
| View follow-up details | `[x]` | View-only mode with date and follow-up number displayed |
| Edit follow-up | `[x]` | Edit button unlocks fields; Update persists changes |
| Delete follow-up | `[ ]` | No DAO method or UI |
| Follow-up numbering | `[~]` | Auto-incremented from total count + 1; breaks if any follow-up is deleted |
| Follow-up ordering | `[~]` | `ORDER BY follow_up_num DESC` on TEXT column — lexicographic order breaks after "9" |

---

## Patient History View

| Feature | Status | Notes |
|---|---|---|
| Patient summary card | `[x]` | Name, occupation, regno in `PatientHistoryActivity` |
| Follow-up list | `[x]` | Dynamically inflated scroll view |
| Navigate to follow-up detail | `[x]` | Click follow-up row → `PatientFollowUpActivity` view mode |
| Navigate to initial patient details | `[x]` | Button → `AddPatientActivity` view mode |
| Add follow-up from history | `[x]` | "Add Follow-up" button → `PatientFollowUpActivity` add mode |

---

## Dashboard

| Feature | Status | Notes |
|---|---|---|
| Patient count — today | `[x]` | LiveData from `getPatientsFromDay(get1DayAgo())` |
| Patient count — week | `[x]` | 7-day window |
| Patient count — month | `[x]` | 31-day window |
| Patient count — year | `[x]` | 365-day window |
| Follow-up count — today / week / month / year | `[x]` | Same pattern on `follow_up_data` |
| Patients added last week list | `[x]` | StateFlow → dynamically inflated scroll view |
| Patients with follow-ups last week list | `[x]` | INNER JOIN query → dynamically inflated |
| Real-time clock | `[x]` | Updated every second via `Handler.postDelayed` |
| Doctor details section | `[ ]` | Mentioned in architecture notes; not implemented |
| Earnings / revenue summary | `[ ]` | `paid` field exists on Patient and FollowUp; no aggregation |
| Charts / graphs | `[ ]` | Not implemented |

---

## Firebase Backup / Sync

| Feature | Status | Notes |
|---|---|---|
| Manual sync button | `[x]` | "Sync to Cloud" in `BackUpActivity` |
| Upload patients to Firebase | `[x]` | Per-child `setValue()` for each patient |
| Download patients from Firebase | `[x]` | Single-event listener |
| Upload follow-ups to Firebase | `[x]` | Per-child `setValue()` |
| Download follow-ups from Firebase | `[x]` | Single-event listener |
| Bidirectional merge | `[~]` | Logic exists; timestamp comparison is buggy (string vs Long) |
| Auto / background sync | `[ ]` | No WorkManager; manual only |
| Sync progress indicator | `[ ]` | No progress UI; only a Toast on completion |
| Sync error feedback | `[ ]` | Firebase errors silently produce `emptyList()` |
| Import local DB from file | `[ ]` | Planned; not started |
| Export local DB to file | `[ ]` | Planned; not started |

---

## Navigation

| Feature | Status | Notes |
|---|---|---|
| Navigation drawer | `[x]` | 50% screen width; all top-level screens |
| Material Toolbar | `[x]` | White hamburger icon on all screens |
| Back navigation | `[x]` | Drawer closes on back press; standard Android back stack otherwise |
| Deep linking | `[ ]` | Not implemented |
| Jetpack Navigation Component | `[ ]` | Not used; explicit intents only |

---

## Offline Support

| Feature | Status | Notes |
|---|---|---|
| Offline data access (Room) | `[x]` | All reads and writes go to local Room DB first |
| Offline add patient | `[x]` | Works without network |
| Offline add follow-up | `[x]` | Works without network |
| Sync on network restore | `[ ]` | No network state listener or WorkManager job |

---

## Non-functional

| Feature | Status | Notes |
|---|---|---|
| ProGuard / R8 minification | `[ ]` | `isMinifyEnabled = false` on release builds |
| Unit tests | `[ ]` | Placeholder files only |
| Instrumented / Espresso tests | `[ ]` | Placeholder files only |
| Dark mode | `[ ]` | Not implemented |
| Tablet layout | `[ ]` | No alternative res/ layouts |
| Accessibility (content descriptions) | `[ ]` | Not assessed |
| Crash reporting (Crashlytics) | `[ ]` | Not integrated |
| Analytics | `[ ]` | Not integrated |

---

## Planned Features (from Architecture of Clinic App.txt)

| Feature | Priority |
|---|---|
| Real Firebase Authentication | Critical |
| Logout implementation | High |
| Delete patient + follow-ups | High |
| Import / Export local DB | High |
| Fix bidirectional sync (Long comparison) | High |
| Fix `toInt()` crash on empty fields | High |
| Fix random ID generation (use autoGenerate) | High |
| Fix `dateJoined` overwrite on patient edit | High |
| Follow-up numbering via MAX instead of COUNT | Medium |
| Pagination / lazy loading of patient list | Medium |
| Background auto-sync (WorkManager) | Medium |
| Earnings summary on Dashboard | Medium |
| Doctor details on Dashboard | Low |
| Voice assist | Low |
| Charts on Dashboard | Low |
