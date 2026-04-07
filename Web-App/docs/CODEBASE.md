# Codebase — Web-App

## `includes/connection.php`

Establishes the MySQLi connection used by every PHP file.

```php
$conn = mysqli_connect("localhost", "root", "", "hospital");
```

- Database host: `localhost`
- User: `root` with **no password** — critical for any non-local deployment
- Database: `hospital`
- No `mysqli_set_charset()` call — character encoding is undefined
- No error suppression or graceful fallback; a failed connection terminates with a raw error

---

## `includes/header.php`

Included at the top of every authenticated page. Responsibilities:
1. `session_start()` — starts or resumes the PHP session.
2. Checks `$_SESSION['adminId']`; redirects to `index.php` if not set.
3. Renders the Bootstrap + MDB navigation bar with links to Dashboard, Home, Add Patient, Records, Detailed Records.
4. Includes a search form (POST to `search.php`).

**Dependencies**: Bootstrap 4.3.1, MDBootstrap, Font Awesome 5.8.2, jQuery 3.4.1.

---

## `index.php`

Login form page. No server-side logic — submits via POST to `includes/logincheck.php`.

Fields: email/username (`mailuid`), password (`pwd`).  
Layout: two-column Bootstrap grid — doctor illustration on left, login form on right.

---

## `includes/logincheck.php`

POST handler for the login form.

```php
$sql = "SELECT * FROM admin_users WHERE email_admin=? OR uid_admin=?";
// Prepared statement — correct
$stmt = mysqli_stmt_init($conn);
mysqli_stmt_bind_param($stmt, "ss", $mailuid, $mailuid);
// ...
if ($password != $row['password_admin']) { // plaintext comparison — wrong
```

- Uses a **prepared statement** for the SELECT (correct).
- Does **plaintext password comparison** — no `password_verify()`.
- Email sanitisation and validation are commented out.
- On success: sets `$_SESSION['adminId']`, `adminUid`, `firstName`, `lastName`; redirects to `home.php`.
- `session_start()` is called inside a conditional block, not at the top of the file.

---

## `home.php`

Post-login landing page. Two Bootstrap jumbotron cards:
- **View Records** → `records.php`
- **Add Patient** → `fillForm.php`

Includes `header.php` (auth guard) and `footer.php`.

---

## `fillForm.php`

Full patient intake form. The largest front-end file (~11 KB).

Sections:
1. Demographics: First, Middle, Last name, Age, Sex, Occupation
2. Contact: Address, Phone, Registration Number
3. Vitals: Height, Weight, Diagnosis
4. Chief Complaints: CC1, CC2, CC3
5. Homeopathic history: Appetite, Desire, Aversions, Thirst, Perspiration, Sleep, Stool, Urine, Menses, Thermal, Mind
6. Case particulars: Hobbies, Particulars, On Examination, Path Investigations, Previous Rx, Past History, Family History
7. Treatment plan: Treatment, Paid, Balance
8. Follow-up dates: FollowUp1–FollowUp4 (legacy fields)

Form action: `php/insertRecord.php` via GET.

Voice input: `js/script.js` (Annyang) is loaded and active on this page.

On successful insert: page reloads with `?insert=success` and shows a success message.

---

## `php/insertRecord.php`

Reads all 40+ patient fields from `$_GET` and executes a raw INSERT:

```php
$firstName = $_GET['firstName'];  // no sanitisation
// ...
$sql = "INSERT INTO patient_data (...) VALUES ('$firstName', '$lastName', ...)";
// direct string interpolation — SQL injection vulnerability
mysqli_query($conn, $sql);
```

- **Critical**: SQL injection via every field.
- No input validation (type, length, format).
- `followUp1`–`followUp4` are stored as `isset($_GET['followUpN'])` — a boolean cast to `"1"`/`""`, not actual dates.
- On success: redirects to `fillForm.php?insert=success`.
- On failure: echoes the raw SQL and `$conn->error` to the browser.

---

## `records.php`

Paginated patient list. Includes `includes/filter.php` for sort and pagination logic.

Displays per row: Registration Number, Name, Date Joined, Address, Phone, Paid, Balance, Treatment, Action.

"Action" links to `patientDetails.php?id=<id>`.

Pagination: 20 records per page with next/previous controls.

---

## `includes/filter.php`

Builds the WHERE, ORDER BY, and LIMIT/OFFSET clauses dynamically.

Sort options (from a GET parameter): `fname`, `lname`, `datejoined`, `regno`, `diagnosis`.

```php
$sql .= "ORDER BY $sort ";  // $sort is from $_GET — SQL injection risk
```

The `$sort` value is chosen from a client-side dropdown, but the server trusts the raw parameter value without whitelisting.

Pagination: `LIMIT 20 OFFSET ($page - 1) * 20`.

---

## `patientDetails.php`

Displays a single patient's full details and all follow-ups.

```php
$sql = "SELECT * FROM patient_data WHERE id = '".$_GET['id']."'";
// SQL injection via id parameter
```

Layout:
- Patient name and registration number in a card.
- "Add Follow-up" button → `followUp.php?id=<id>`.
- Accordion for each follow-up (date, number, all fields).
- Collapsible "Initial Details" section (renders `includes/patientDetailsForm.php`).

Database output is echoed directly to HTML without `htmlspecialchars` — XSS risk.

---

## `patientDetailsEdit.php`

Edit form for an existing patient. Pre-fills all fields from the database using the `id` GET parameter. Submits to `php/updateRecord.php` via GET.

Same SQL injection and XSS risks as `patientDetails.php`.

---

## `php/updateRecord.php`

Reads all patient fields from `$_GET` and builds a raw UPDATE:

```php
$sql = "UPDATE patient_data SET firstName='$firstName', ... WHERE id='$id'";
```

Same SQL injection vulnerability as `insertRecord.php`. No prepared statements. On success: redirects to `patientDetails.php?insert=success`.

---

## `followUp.php`

Add follow-up form for a specific patient. Retrieves patient data by `$_GET['id']` to pre-fill read-only fields (Name, RegNo, Phone, ID). Editable fields: Weight, Treatment Output, Other Complaints, Treatment, Medicine Duration, Paid, Balance.

Submits via GET to `php/insertFollowUp.php`.

---

## `php/insertFollowUp.php`

Calculates the next follow-up number and inserts a new follow-up record.

```php
// Finds the max existing follow_up_num for this patient
$sql_max = "SELECT MAX(follow_up_num) as max_num FROM follow_up_data WHERE id = '$id'";
$follow_up_num = $max_num + 1;

// Inserts
$sql = "INSERT INTO follow_up_data (...) VALUES ('$id', ..., '$follow_up_num')";
```

**Bugs**:
1. `MAX()` on `VARCHAR` column uses lexicographic ordering — `MAX("1","2","9","10")` returns `"9"`, not `"10"`. Follow-up numbering breaks after 9.
2. SQL injection via all GET parameters.
3. No DB transaction between the SELECT MAX and INSERT — race condition possible.

---

## `php/deleteRecord.php`

Deletes a patient by ID.

```php
$sql = "DELETE FROM patient_data WHERE id = '$id'";
```

- SQL injection via `id`.
- **No cascade**: associated `follow_up_data` rows are not deleted, creating orphaned records.

---

## `dashboard.php`

Main statistics dashboard. Includes `includes/earning.php` which executes 12 aggregate SQL queries producing 16 PHP variables.

Layout:
- Doctor profile card (Name, DOB, Degree, Email from session/hardcoded).
- Four summary cards: Today, Week, Month, Year — each showing new patients, follow-ups, and earnings.
- Table: Patients added last week (with "View Report" links).
- Table: Follow-up patients last week (INNER JOIN on `patient_data` + `follow_up_data`).
- Real-time clock (from `includes/clock.php`).

---

## `includes/earning.php`

Provides 16 variables to `dashboard.php` by running COUNT and SUM queries across four time windows.

```php
$todayNew    = "SELECT COUNT(id) as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 0 DAY)";
$weekEarnNew = "SELECT SUM(paid) as count FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
// ... 10 more similar queries
```

`SUM(paid)` and `SUM(balance)` operate on `VARCHAR` columns. MySQL silently coerces non-numeric strings to `0` — earnings totals can be silently wrong.

---

## `search.php`

Receives a POST from the navbar search form. Executes a LIKE query against `patient_data`. Displays matching records.

---

## `detailedRecords.php`

Extended records view; includes additional columns or a different layout compared to `records.php`.

---

## `updateRecord.php`

Entry point for the patient edit flow (not to be confused with `php/updateRecord.php`). Loads the patient by ID and renders the edit form.

---

## `js/script.js`

Annyang speech recognition integration for `fillForm.php`.

Defines voice commands:

| Voice command | Action |
|---|---|
| `"hello"` | Alert greeting |
| `"what is this"` | Alert description |
| `"reload the page"` | `location.reload()` |
| `"stop listening"` | `annyang.abort()` |
| `"submit"` | `$('#submitBtn').click()` |
| `"first name *val"` | Sets `#firstName` input |
| `"middle name *val"` | Sets `#middleName` input |
| `"last name *val"` | Sets `#lastName` input |
| `"age *val"` | Sets `#age` input |
| `"occupation *val"` | Sets `#occupation` |
| ... (per-field commands for all 35+ fields) | Sets corresponding input |

Uses continuous listening mode; auto-restarts `annyang.start()` on session end.

---

## `css/style.css`

Minimal custom overrides. Most styling is handled by Bootstrap 4 and MDB.

---

## `setup/`

Contains SQL setup scripts for creating the `hospital` database and tables. Not version-controlled as migrations — manual baseline only.

---

## Legacy / Unused Files

| File | Status |
|---|---|
| `patientDetailsOLD.php` | Old version of patientDetails; superseded by current `patientDetails.php` |
| `testSideNavForm.php` | UI prototype / scratch file; not linked from any navigation |

---

## External Dependencies

| Library | Version | Purpose |
|---|---|---|
| Bootstrap | 4.3.1 | CSS grid, components |
| Material Design Bootstrap (MDB) | — | Extended Material components |
| jQuery | 3.4.1 | DOM manipulation, AJAX |
| Font Awesome | 5.8.2 | Icons |
| Annyang | latest (CDN) | Speech recognition |
| SpeechKITT | latest (CDN) | Speech recognition UI overlay |
