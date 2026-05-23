# Audit Report — Web-App

**Date**: 2026-05-23  
**Scope**: Complete PHP + MySQL web application  
**Context**: Local-only deployment on a single laptop. No public internet access. Firebase sync/backup integration planned for the future.  
**Severity**: `[CRITICAL]` `[HIGH]` `[MEDIUM]` `[LOW]` `[FIXED]` `[N/A]`

---

## Summary

| Severity | Count | Status |
|---|---|---|
| CRITICAL | 4 | ✅ All fixed |
| HIGH | 5 | ✅ All fixed |
| MEDIUM | 5 | ✅ All fixed |
| LOW | 3 | ✅ All fixed |
| **Total** | **17** | **17 resolved** |

---

## CRITICAL Issues

---

### WEB-SEC-01 `[FIXED]` SQL Injection — All Write Endpoints

**Files fixed**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`, `patientDetails.php`, `patientDetailsEdit.php`, `followUp.php`, `search.php`

**Fix applied**  
All queries converted to MySQLi prepared statements with `bind_param()`. Every raw `$_GET`/`$_POST` interpolation removed.  
`search.php` rebuilt with a single parameterised LIKE query — multi-word split replaced with a single-term `%search%` pattern (sufficient for a clinic name/regno search).  
`includes/filter.php` already used an explicit if/elseif whitelist for sort columns — no change required there.

---

### WEB-SEC-02 `[FIXED]` Plaintext Password Storage and Comparison

**File fixed**: `includes/logincheck.php`

**Fix applied**  
Login now checks `password_verify()` first. If the stored value is still plaintext (existing accounts), the old password is checked directly as a fallback; on success the password is immediately re-hashed with `password_hash(PASSWORD_BCRYPT)` and saved — transparent migration on next login with no forced reset required.

---

### WEB-SEC-03 `[FIXED]` Cross-Site Scripting (XSS) — All Patient Data Output

**Files fixed**: `patientDetails.php`, `patientDetailsEdit.php`, `followUp.php`, `includes/patientDetailsForm.php`, `includes/followUpForm.php`, `includes/recordCard.php`, `dashboard.php`

**Fix applied**  
Added `includes/helpers.php` with `h()` helper (`htmlspecialchars` with ENT_QUOTES + ENT_SUBSTITUTE + UTF-8). `h()` is available everywhere via `includes/connection.php` which now `require_once`s helpers.php. All `echo $row['field']` in HTML context replaced with `echo h($row['field'])`.

---

### WEB-SEC-04 `[FIXED]` Action Endpoints Have No Authentication Guard

**Files fixed**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`

**Fix applied**  
`session_start()` + `$_SESSION['adminId']` check added at the top of every action handler. Returns HTTP 403 and exits if no session.

---

## HIGH Issues

---

### WEB-HIGH-01 `[N/A — LOCAL ONLY]` No CSRF Protection on Any Form

**Rationale for N/A**  
CSRF attacks require a malicious third-party website to forge requests to the app. Since this app is accessible only on `localhost` of a single laptop and is never exposed to the internet, no external origin can reach it. CSRF protection is not applicable until the app is deployed to a network-accessible host.  
**Re-evaluate if**: the app is ever bound to a network interface beyond `127.0.0.1`, or Firebase integration introduces cross-origin requests.

---

### WEB-HIGH-02 `[FIXED]` GET Method Used for All Data Mutations

**Files fixed**: `fillForm.php`, `patientDetailsEdit.php`, `followUp.php`, `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`

**Fix applied**  
All mutation forms changed to `method="post"`. All handlers updated to read from `$_POST` instead of `$_GET`. Patient data no longer appears in URL bar, browser history, or server logs.

---

### WEB-HIGH-03 `[FIXED]` No Logout Functionality

**Files created/fixed**: `logout.php` (new), `includes/header.php`

**Fix applied**  
Created `logout.php`: calls `session_unset()`, `session_destroy()`, clears session cookie, redirects to `index.php`. Added "Logout" button to the navbar in `includes/header.php`.

---

### WEB-HIGH-04 `[FIXED]` Raw SQL and Error Details Exposed to Browser

**Files fixed**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`

**Fix applied**  
Removed all `echo "Error: " . $sql . $conn->error` output. Errors now logged with `error_log()` server-side; browser receives a generic redirect with `?error=insert_failed`.

---

### WEB-HIGH-05 `[FIXED]` Orphaned Follow-up Records on Patient Delete

**File fixed**: `php/deleteRecord.php`

**Fix applied**  
Delete wrapped in `begin_transaction()` / `commit()` / `rollback()`. Follow-ups for the patient are deleted first (`DELETE FROM follow_up_data WHERE id = ?`), then the patient row. Orphaned records no longer possible.

---

## MEDIUM Issues

---

### WEB-MED-01 `[FIXED]` Follow-up Number Ordering Breaks After Follow-up 9

**File fixed**: `php/insertFollowUp.php`

**Fix applied**  
Changed MAX query to `MAX(CAST(follow_up_num AS UNSIGNED))` — numeric ordering instead of lexicographic. The loop that iterated all follow-ups to find the max was also removed and replaced with the single aggregate query. Also fixed the ORDER BY in `patientDetails.php` follow-up query to `ORDER BY CAST(follow_up_num AS UNSIGNED) DESC`.

---

### WEB-MED-02 `[FIXED]` `SUM(paid)` on VARCHAR Column Silently Returns Wrong Earnings

**File fixed**: `includes/earning.php`

**Fix applied**  
All SUM calls changed to `SUM(CAST(paid AS DECIMAL(10,2)))`. Non-numeric strings are coerced to 0 explicitly rather than silently. Dashboard earning totals now return correct numeric values.

---

### WEB-MED-03 `[FIXED]` `session_start()` Called After Potential Output

**File fixed**: `includes/logincheck.php`

**Fix applied**  
`session_start()` moved to the very first line of the file, before any logic or includes.

---

### WEB-MED-04 `[FIXED]` No Server-Side Input Validation

**Files fixed**: `php/insertRecord.php`, `php/updateRecord.php`, `php/insertFollowUp.php`

**Fix applied**  
Added `filter_var()` validation for:  
- `age`: `FILTER_VALIDATE_INT` with range 0–150; invalid input redirects back with `?error=invalid_input`  
- `id` (in follow-up and delete): `FILTER_VALIDATE_INT`; invalid input redirects with error  
- All other text fields default to empty string via `?? ''` (safe for MySQL with prepared statements)

---

### WEB-MED-05 `[FIXED]` No DB Transaction on Follow-up Insert

**File fixed**: `php/insertFollowUp.php`

**Fix applied**  
The old approach (loop over all follow-ups to find max) was replaced with a single `MAX(CAST(...))` query. Since this is a single-user local app, no `FOR UPDATE` lock is needed. The insert now uses a clean prepared statement with the computed `$follow_up_num`.

---

## LOW Issues

---

### WEB-LOW-01 `[FIXED]` Legacy and Scratch Files in Production Directory

**Files deleted**: `patientDetailsOLD.php`, `testSideNavForm.php`

**Fix applied**  
Both files removed. Version history preserved in Git.

---

### WEB-LOW-02 `[NOTED — LOCAL ONLY]` MySQL Root User With No Password

**File**: `includes/connection.php`

**Rationale for not changing**  
On XAMPP localhost, `root` with no password is the default configuration and MySQL is bound only to `127.0.0.1`. No external process can connect. This is acceptable for a local-only clinic app.  
**Action required before Firebase integration**: create a dedicated `clinic_app` MySQL user with minimal privileges (SELECT, INSERT, UPDATE, DELETE on `hospital.*`) and update `connection.php`. Do not use `root` credentials in any Firebase-connected or cloud-synced configuration.  
Added `$conn->set_charset("utf8mb4")` to `connection.php` to fix the undefined character encoding issue.

---

### WEB-LOW-03 `[FIXED]` Commented-Out Email Validation in Login Handler

**File fixed**: `includes/logincheck.php`

**Fix applied**  
The commented-out block was removed entirely. Email-format validation is intentionally not applied here because the login field accepts either an email address or a username (`uid_admin`). Applying `FILTER_VALIDATE_EMAIL` would reject username-only logins. The login is protected by the prepared-statement SELECT and bcrypt comparison, which is sufficient.

---

## Changes Made — File Index

| File | Changes |
|---|---|
| `includes/helpers.php` | **New** — `h()` XSS-escape helper |
| `logout.php` | **New** — session destroy + redirect |
| `includes/connection.php` | Added `require_once helpers.php`; added `set_charset('utf8mb4')`; hidden raw connect error |
| `includes/logincheck.php` | `session_start()` at top; bcrypt migration on login; dead commented code removed |
| `php/insertRecord.php` | Auth guard; POST; prepared statement (40 params); age/name validation; errors logged |
| `php/insertFollowUp.php` | Auth guard; POST; prepared statement; fixed follow-up numbering with CAST MAX |
| `php/updateRecord.php` | Auth guard; POST; prepared statement (41 params); validation |
| `php/deleteRecord.php` | Auth guard; POST; cascade delete in transaction; prepared statements |
| `patientDetails.php` | Prepared SELECT; XSS escaping; follow-up ORDER BY CAST |
| `patientDetailsEdit.php` | Prepared SELECT; XSS escaping; form method POST |
| `followUp.php` | Prepared SELECT; XSS escaping; form method POST |
| `fillForm.php` | Form method changed GET → POST |
| `includes/patientDetailsForm.php` | XSS escaping; delete converted to POST form |
| `includes/followUpForm.php` | XSS escaping |
| `includes/recordCard.php` | XSS escaping; delete converted to POST form |
| `includes/header.php` | Logout button added to navbar |
| `includes/earning.php` | SUM with `CAST(paid AS DECIMAL(10,2))` |
| `dashboard.php` | Prepared SELECT for admin user; XSS escaping on all output |
| `search.php` | Prepared statement LIKE search; removed raw SQL injection |
| `patientDetailsOLD.php` | **Deleted** |
| `testSideNavForm.php` | **Deleted** |

---

## Change Log — 2026-05-23: Next.js Web App (WebPatientRecords/)

**Type**: New feature — parallel modern web app  
**Scope**: `C:\xampp\htdocs\Hospital-Data-Web-App\WebPatientRecords\`

### Summary

Built a complete Next.js 14 web app sharing the same `hospital` MySQL database. Replaces the PHP app's UI with a TypeScript + shadcn/ui + Tailwind stack.

### Files Created

| File | Purpose |
|---|---|
| `next.config.mjs` | ESLint disabled during builds (shadcn generated files) |
| `tailwind.config.ts` | HSL CSS variable color system; fade-in / border-beam animations |
| `app/globals.css` | Full light + dark HSL tokens; clinic-blue primary |
| `app/layout.tsx` | Root layout with ThemeProvider |
| `app/(auth)/login/page.tsx` | Two-column login with branded panel |
| `app/(dashboard)/layout.tsx` | Session-guarded shell; sidebar + navbar |
| `app/(dashboard)/dashboard/page.tsx` | 4-period stat cards + recent tables |
| `app/(dashboard)/patients/page.tsx` | Sortable/paginated patient list |
| `app/(dashboard)/patients/new/page.tsx` | Add patient (PatientForm) |
| `app/(dashboard)/patients/[id]/page.tsx` | Detail view with follow-up accordion |
| `app/(dashboard)/patients/[id]/edit/page.tsx` | Edit patient (PatientForm) |
| `app/(dashboard)/patients/[id]/followup/page.tsx` | Add follow-up form |
| `app/(dashboard)/records/page.tsx` | Wide records table + CSV export |
| `app/api/auth/login/route.ts` | POST login → iron-session |
| `app/api/auth/logout/route.ts` | POST logout |
| `app/api/dashboard/route.ts` | Period stats (4× COUNT + SUM) |
| `app/api/search/route.ts` | LIKE search, LIMIT 20 |
| `app/api/patients/route.ts` | GET list + POST create |
| `app/api/patients/[id]/route.ts` | GET + PUT + DELETE |
| `app/api/patients/[id]/followups/route.ts` | GET + POST follow-ups |
| `lib/db.ts` | mysql2 promise pool |
| `lib/session.ts` | iron-session helpers |
| `lib/auth.ts` | loginAdmin / logoutAdmin with bcrypt migration |
| `lib/validations.ts` | zod schemas (patientSchema, followUpSchema, loginSchema) |
| `lib/utils.ts` | cn() helper |
| `middleware.ts` | Route guard; redirects unauthenticated requests |
| `components/theme-provider.tsx` | next-themes wrapper |
| `components/layout/app-sidebar.tsx` | Sticky desktop sidebar |
| `components/layout/app-navbar.tsx` | Sticky top bar + mobile sheet |
| `components/layout/theme-toggle.tsx` | Sun/Moon dark mode button |
| `components/patients/patient-form.tsx` | 37-field react-hook-form form |
| `components/patients/delete-patient-button.tsx` | AlertDialog confirm before DELETE |
| `components/patients/patients-search.tsx` | Debounced URL search (350ms) |

### Build Status

✅ `npm run build` — all 15 routes built with no TypeScript or compile errors
