# Features — Hospital Data Web App

Status legend:  
`[x]` Implemented  `[~]` Partially implemented  `[ ]` Planned / not started

---

## PatientRecords — Android App

### Authentication
| Feature | Status | Notes |
|---|---|---|
| Login screen | `[x]` | Hardcoded credentials (email: `a`, password: `a`) |
| Session persistence | `[ ]` | No SharedPreferences or token storage; re-login required every launch |
| Logout | `[~]` | Menu item exists; handler body is empty — does nothing |
| Real authentication (Firebase/backend) | `[ ]` | Not implemented in PatientRecords app |

### Patient Management
| Feature | Status | Notes |
|---|---|---|
| Add new patient | `[x]` | 35+ fields covering full homeopathic case history |
| View patient list | `[x]` | RecyclerView with name, sex, occupation, phone, regno |
| Search patients | `[x]` | Debounced (300ms) search across first, middle, last name |
| View patient details | `[x]` | Read-only mode in `AddPatientActivity` |
| Edit patient details | `[x]` | Edit mode unlocks all fields; Update button persists changes |
| Delete patient | `[ ]` | `PatientDao.delete()` exists but no UI exposes it |
| Patient photo / image | `[~]` | `urlToImage` field in data model; no camera/gallery UI implemented |
| Patient registration number | `[x]` | Manual entry field (`regno`); no auto-generation |
| Pagination / lazy loading | `[ ]` | All patients loaded at once |
| Export patient data | `[ ]` | Planned; not started |

### Follow-up Management
| Feature | Status | Notes |
|---|---|---|
| Add follow-up for a patient | `[x]` | Weight, treatment output, complaints, treatment, medicine duration, paid, balance |
| View follow-up details | `[x]` | View-only mode with date and follow-up number displayed |
| Edit follow-up | `[x]` | Edit button unlocks fields; Update persists changes |
| Delete follow-up | `[ ]` | No DAO method or UI for deletion |
| Follow-up numbering | `[x]` | Auto-incremented from total count + 1; fragile (see audit) |
| Follow-up ordering | `[x]` | Ordered DESC by `follow_up_num` in DAO query |

### Dashboard
| Feature | Status | Notes |
|---|---|---|
| Today / week / month / year patient counts | `[x]` | 4× LiveData counts displayed as summary cards |
| Today / week / month / year follow-up counts | `[x]` | 4× LiveData counts displayed as summary cards |
| Patients added last week list | `[x]` | Dynamically inflated in ScrollView |
| Follow-up patients last week list | `[x]` | INNER JOIN query; dynamically inflated |
| Real-time clock | `[x]` | Updated every second via `Handler.postDelayed` |
| Doctor details section | `[ ]` | Planned in architecture doc; not implemented |
| Earnings / revenue summary | `[ ]` | `paid` field exists but no aggregation in Android app |

### Firebase Sync (Backup)
| Feature | Status | Notes |
|---|---|---|
| Manual sync to Firebase | `[x]` | Single button in `BackUpActivity` |
| Upload patients to Firebase | `[x]` | `FirebaseRepository.uploadPatients()` |
| Download patients from Firebase | `[x]` | `FirebaseRepository.downloadPatients()` |
| Upload follow-ups to Firebase | `[x]` | `FirebaseRepository.uploadPatientFollowUps()` |
| Download follow-ups from Firebase | `[x]` | `FirebaseRepository.downloadFollowUps()` |
| Bidirectional merge sync | `[~]` | Implemented but uses broken string-based timestamp comparison |
| Automatic / background sync | `[ ]` | No WorkManager or periodic sync |
| Import local DB from file | `[ ]` | Planned; not started |
| Export local DB to file | `[ ]` | Planned; not started |
| Sync progress indicator | `[ ]` | No progress UI; completion shown only via Toast |
| Conflict resolution UI | `[ ]` | Silently overwritten by timestamp heuristic |

### Navigation
| Feature | Status | Notes |
|---|---|---|
| Navigation drawer | `[x]` | 50% screen width; Home, Add Patient, View Patients, Dashboard, Backup, Logout |
| Material Toolbar | `[x]` | White hamburger icon on all BaseActivity screens |
| Back navigation | `[x]` | Drawer closes on back press; otherwise default back stack |
| Deep linking | `[ ]` | Not implemented |

### Voice Input
| Feature | Status | Notes |
|---|---|---|
| Voice input (Android) | `[ ]` | No voice input on Android app; only in web app |

### Miscellaneous
| Feature | Status | Notes |
|---|---|---|
| Offline-first (Room DB) | `[x]` | All data persisted locally; works without network |
| Dark mode | `[ ]` | Not implemented |
| Tablet / large screen layout | `[ ]` | No alternative layouts |
| Localisation | `[ ]` | Hardcoded English strings; no string resource externalisation for data labels |

---

## Mahajan Homeo Clinic — Java Legacy App

| Feature | Status | Notes |
|---|---|---|
| Email / password login | `[x]` | Firebase Authentication |
| Email verification | `[x]` | Sent on registration; checked on login |
| User registration | `[x]` | Name, email, phone, organisation, password |
| Phone OTP verification | `[x]` | Firebase Phone Auth; hardcoded +91 country code |
| Admin approval workflow | `[ ]` | Placeholder message only; no backend logic |
| Dashboard | `[~]` | Activity exists; no content |
| Patient management | `[ ]` | Not implemented in this app |

---

## Web-App — PHP Admin Interface

### Authentication
| Feature | Status | Notes |
|---|---|---|
| Admin login | `[x]` | Email or username + plaintext password |
| Session management | `[x]` | PHP sessions; redirects to login if no session |
| Logout | `[ ]` | No logout endpoint or session_destroy call found |
| Registration | `[ ]` | Admin accounts must be created directly in DB |

### Patient Management
| Feature | Status | Notes |
|---|---|---|
| Add new patient | `[x]` | Full homeopathic case intake form with voice input |
| View patient list | `[x]` | Paginated (20/page) with sort options |
| View patient details | `[x]` | Accordion with initial details + all follow-ups |
| Edit patient details | `[x]` | `patientDetailsEdit.php` + `php/updateRecord.php` |
| Delete patient | `[x]` | `php/deleteRecord.php`; no cascade to follow_up_data |
| Search patients | `[x]` | POST form submitting to `search.php` |
| Sort / filter | `[x]` | firstName, lastName, dateJoined, regno, diagnosis |
| Pagination | `[x]` | 20 records per page with offset |
| Detailed records view | `[x]` | `detailedRecords.php` |

### Follow-up Management
| Feature | Status | Notes |
|---|---|---|
| Add follow-up | `[x]` | `followUp.php` + `php/insertFollowUp.php` |
| View follow-ups | `[x]` | Accordion in `patientDetails.php` |
| Edit follow-up | `[ ]` | No edit page for follow-ups |
| Delete follow-up | `[ ]` | No delete endpoint for follow-ups |

### Dashboard
| Feature | Status | Notes |
|---|---|---|
| Today / week / month / year counts | `[x]` | Patients added + follow-ups added per period |
| Earnings summary | `[x]` | SUM(paid) per period for both new patients and follow-ups |
| Patients added last week table | `[x]` | With "View Report" links |
| Follow-ups last week table | `[x]` | INNER JOIN on patient_data + follow_up_data |
| Real-time clock | `[x]` | Via `includes/clock.php` |
| Doctor details | `[x]` | Name, DOB, Degree, Email displayed in sidebar |

### Voice Input
| Feature | Status | Notes |
|---|---|---|
| Voice commands for form fields | `[x]` | Annyang + SpeechKITT; "first name John", "age 45", etc. |
| Voice navigation commands | `[~]` | `reload the page`, `stop listening`, `submit` commands wired |
| Continuous listening mode | `[x]` | Auto-restarts on speech session end |

### Other
| Feature | Status | Notes |
|---|---|---|
| Responsive design | `[x]` | Bootstrap 4 grid |
| Mobile-optimised web | `[~]` | Bootstrap responsive but not tested on mobile |
| Data export (CSV/PDF) | `[ ]` | Not implemented |
| Backup / import | `[ ]` | No export/import functionality |
| Multi-doctor support | `[ ]` | Single admin user; no multi-tenancy |
| Audit log | `[ ]` | No change tracking |

---

## Feature Roadmap (from Architecture of Clinic App.txt)

The following features were listed as planned in the project planning document:

| Feature | Target Sub-system | Priority |
|---|---|---|
| Backup screen (import/export local DB) | Android | High |
| Delete patient (record + all follow-ups) | Android | High |
| Delete follow-up | Android | High |
| Pagination / lazy loading | Android | Medium |
| Advanced filter functionality | Android | Medium |
| Offline-first sync on internet restore | Android | Medium |
| Voice assist for navigation | Android | Low |
| Doctor details on dashboard | Android | Low |
| Register/login update (real auth) | Android | High |
| Firebase sync conflict UI | Android | Medium |
| Edit follow-up | Web-App | Medium |
| Admin logout | Web-App | High |
| Logout (Android) | Android | High |
| Password hashing | Web-App | Critical |
| SQL injection fixes | Web-App | Critical |
