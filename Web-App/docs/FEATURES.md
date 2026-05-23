# Features — Web-App

Status legend:  
`[x]` Implemented  `[~]` Partially implemented  `[ ]` Planned / not started

---

## Authentication & Session

| Feature | Status | Notes |
|---|---|---|
| Admin login (email or username) | `[x]` | Prepared-statement SELECT; bcrypt `password_verify()` with transparent migration from plaintext |
| Session persistence across pages | `[x]` | PHP session; `header.php` guard on all auth pages |
| Logout | `[x]` | `logout.php` — session destroy + cookie clear; logout button in navbar |
| Admin self-registration | `[ ]` | Accounts must be created directly in the DB |
| Password reset | `[ ]` | Not implemented |
| Multi-admin support | `[~]` | Schema supports multiple `admin_users` rows; no role separation |

---

## Patient Management

| Feature | Status | Notes |
|---|---|---|
| Add new patient | `[x]` | Full 35+ field intake form in `fillForm.php` |
| View patient list | `[x]` | Paginated (20/page), sortable |
| View patient details | `[x]` | All fields + follow-ups accordion in `patientDetails.php` |
| Edit patient details | `[x]` | `patientDetailsEdit.php` → `php/updateRecord.php` |
| Delete patient | `[x]` | `php/deleteRecord.php`; no cascade to follow-up records |
| Search patients | `[x]` | Navbar search POST → `search.php`; LIKE query |
| Sort patients | `[x]` | firstName, lastName, dateJoined, regno, diagnosis |
| Paginate patient list | `[x]` | 20 records per page with next/previous |
| Detailed records view | `[x]` | `detailedRecords.php` — extended column view |
| Patient registration number | `[x]` | Manual entry; no auto-generation |
| Patient photo upload | `[ ]` | Not implemented |
| Bulk import / CSV upload | `[ ]` | Not implemented |
| Export patient list (CSV/PDF) | `[ ]` | Not implemented |
| Audit trail / change history | `[ ]` | Not implemented |

---

## Follow-up Management

| Feature | Status | Notes |
|---|---|---|
| Add follow-up for a patient | `[x]` | `followUp.php` → `php/insertFollowUp.php` (POST) |
| View all follow-ups per patient | `[x]` | Accordion in `patientDetails.php`, ordered by `CAST(follow_up_num AS UNSIGNED) DESC` |
| Auto-increment follow-up number | `[x]` | Uses `MAX(CAST(follow_up_num AS UNSIGNED))` — fixed ordering bug that broke after #9 |
| Edit existing follow-up | `[ ]` | No edit form or update endpoint for follow-ups |
| Delete follow-up | `[ ]` | No delete endpoint (follow-ups are cascade-deleted when patient is deleted) |

---

## Dashboard & Statistics

| Feature | Status | Notes |
|---|---|---|
| New patients count — today | `[x]` | `COUNT` with `DATE_SUB(CURDATE(), INTERVAL 0 DAY)` |
| New patients count — week | `[x]` | 7-day window |
| New patients count — month | `[x]` | 30-day window |
| New patients count — year | `[x]` | 365-day window |
| Follow-ups count — today / week / month / year | `[x]` | Same pattern on `follow_up_data` |
| Earnings summary — today / week / month / year | `[x]` | `SUM(paid)` from both tables; silently wrong for VARCHAR values |
| Patients added last week — table | `[x]` | With "View Report" links |
| Follow-up patients last week — table | `[x]` | INNER JOIN `patient_data` + `follow_up_data` |
| Real-time clock | `[x]` | `includes/clock.php` |
| Doctor profile section | `[x]` | Name, DOB, Degree, Email in sidebar |
| Charts / graphs | `[ ]` | Not implemented |
| Revenue trends over time | `[ ]` | Not implemented |
| Appointment calendar | `[ ]` | Not implemented |

---

## Voice Input

| Feature | Status | Notes |
|---|---|---|
| Voice commands for patient form fields | `[x]` | Annyang; "first name John", "age 45", etc. for all 35+ fields |
| Voice submit | `[x]` | "submit" triggers submit button click |
| Voice page navigation | `[~]` | "reload the page", "stop listening" only |
| Continuous listening mode | `[x]` | Auto-restarts on session end |
| SpeechKITT visual overlay | `[x]` | Provides push-to-talk / always-on UI |
| Voice for follow-up form | `[ ]` | Script only loaded on `fillForm.php` |

---

## UI & Accessibility

| Feature | Status | Notes |
|---|---|---|
| Responsive layout (Bootstrap 4) | `[x]` | Works at various screen widths |
| Mobile-optimised layout | `[~]` | Bootstrap responsive but not specifically designed/tested for mobile |
| Dark mode | `[ ]` | Not implemented |
| Accessibility (ARIA labels) | `[ ]` | Not assessed; Bootstrap provides basic defaults |

---

## Security Features

| Feature | Status | Notes |
|---|---|---|
| SQL injection protection | `[x]` | All queries use MySQLi prepared statements with `bind_param()` |
| Password hashing | `[x]` | `password_hash(PASSWORD_BCRYPT)`; transparent migration from plaintext on login |
| CSRF protection | `[N/A]` | Local-only app; no external origin can reach localhost — re-evaluate if deployed to network |
| Input validation (server-side) | `[~]` | `age` and `id` validated; other text fields use `?? ''` safe defaults |
| XSS output escaping | `[x]` | `h()` helper (`htmlspecialchars`) applied to all DB-sourced HTML output |
| HTTPS enforcement | `[ ]` | Not configured (hosting-dependent) |
| Session auth guard on action endpoints | `[x]` | All `php/*.php` handlers check `$_SESSION['adminId']` |
| Data mutations use POST | `[x]` | All forms changed from GET to POST |
| Rate limiting on login | `[ ]` | Not implemented |
| Brute-force protection | `[ ]` | Not implemented |

---

## Planned / Known Missing Features (from project notes)

| Feature | Priority |
|---|---|
| Admin logout | High |
| Password hashing (bcrypt) | Critical |
| Prepared statements on all queries | Critical |
| XSS escaping on all output | Critical |
| Edit and delete follow-up | Medium |
| Cascade delete follow-ups with patient | High |
| Data export (CSV / PDF) | Medium |
| Charts on dashboard | Low |
| Multi-doctor / role separation | Low |
| Patient photo management | Low |
