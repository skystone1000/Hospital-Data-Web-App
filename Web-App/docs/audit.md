# Audit Report — Web-App

**Date**: 2026-05-23  
**Scope**: Complete PHP + MySQL web application  
**Severity**: `[CRITICAL]` `[HIGH]` `[MEDIUM]` `[LOW]`

---

## Summary

| Severity | Count |
|---|---|
| CRITICAL | 4 |
| HIGH | 5 |
| MEDIUM | 5 |
| LOW | 3 |
| **Total** | **17** |

---

## CRITICAL Issues

---

### WEB-SEC-01 `[CRITICAL]` SQL Injection — All Write Endpoints

**Files**: `php/insertRecord.php` (line 49), `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`, `patientDetails.php` (line 8), `patientDetailsEdit.php`, `includes/filter.php`

**Description**  
Every data-write endpoint and several read pages build SQL by directly interpolating unescaped `$_GET` parameters:

```php
// php/insertRecord.php – line 49
$sql = "INSERT INTO patient_data (...) VALUES (
    '$firstName', '$lastName', '$age', ...
)";

// patientDetails.php – line 8
$sql = "SELECT * FROM patient_data WHERE id = '".$_GET['id']."'";

// includes/filter.php
$sql .= "ORDER BY $sort ";   // $sort is a raw GET parameter
```

An attacker can pass `' OR '1'='1` to read the whole table, `'; DROP TABLE patient_data; --` to destroy data, or `UNION SELECT` to extract `admin_users` credentials.

**Fix**  
Convert every query to a MySQLi prepared statement. The login query (`includes/logincheck.php`) already demonstrates the correct pattern — apply it everywhere:

```php
// php/insertRecord.php – replacement pattern
$stmt = $conn->prepare(
    "INSERT INTO patient_data (firstName, lastName, age, sex, ...)
     VALUES (?, ?, ?, ?, ...)"
);
$stmt->bind_param("ssis...", $firstName, $lastName, $age, $sex, ...);
$stmt->execute();
```

For `includes/filter.php`, validate `$sort` against a hardcoded whitelist before using it:

```php
$allowed_sorts = ['firstName', 'lastName', 'dateJoined', 'regno', 'diagnosis'];
$sort = in_array($_GET['sort'] ?? '', $allowed_sorts) ? $_GET['sort'] : 'dateJoined';
```

---

### WEB-SEC-02 `[CRITICAL]` Plaintext Password Storage and Comparison

**File**: `includes/logincheck.php` (line 36), `admin_users` table

**Description**  
Admin passwords are stored as plaintext strings and compared directly:

```php
if ($password != $row['password_admin'])
```

Anyone who reads the `admin_users` table (e.g., via the SQL injection above) immediately has every admin credential.

**Fix**  
Use PHP's built-in bcrypt functions. Existing passwords must be re-hashed — easiest via a forced password reset on first login after migration.

```php
// When setting a password (registration / password change):
$hashed = password_hash($plaintext, PASSWORD_BCRYPT);
// Store $hashed in password_admin column

// On login:
if (!password_verify($password, $row['password_admin'])) {
    header("Location: ../index.php?error=wrongPassword");
    exit();
}
```

Migration script (run once):

```sql
-- Mark all passwords as requiring reset
ALTER TABLE admin_users ADD COLUMN password_reset_required TINYINT(1) DEFAULT 1;
```

---

### WEB-SEC-03 `[CRITICAL]` Cross-Site Scripting (XSS) — All Patient Data Output

**Files**: `patientDetails.php`, `records.php`, `dashboard.php`, `search.php`, all includes that echo DB values

**Description**  
Patient data read from the database is echoed directly into HTML without escaping:

```php
echo $row['firstName'];          // can contain <script>alert(1)</script>
echo $row['address'];            // long text fields — high injection surface
```

Because `php/insertRecord.php` has no input sanitisation either, an attacker who submits a patient record with a `<script>` payload will have that script execute in the browser of every staff member who views that patient's details.

**Fix**  
Wrap every database-sourced echo in `htmlspecialchars()`. Add a project-wide helper to reduce boilerplate:

```php
// At top of connection.php or a new helpers.php
function h(string $s): string {
    return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

// Usage in templates:
echo h($row['firstName']);
echo h($row['address']);
```

---

### WEB-SEC-04 `[CRITICAL]` Action Endpoints Have No Authentication Guard

**Files**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`

**Description**  
All four mutation endpoints include only `connection.php` at the top — no session check. An unauthenticated user who knows the URL can add, modify, or delete patient records without logging in:

```bash
curl "http://example.com/php/deleteRecord.php?id=1"
# Deletes patient with id=1 — no session required
```

`header.php` (which contains the session guard) is only included by page-level files, not by these action handlers.

**Fix**  
Add a session check at the top of each action file:

```php
// Add to top of each php/*.php file
session_start();
if (!isset($_SESSION['adminId'])) {
    http_response_code(403);
    exit('Unauthorized');
}
```

---

## HIGH Issues

---

### WEB-HIGH-01 `[HIGH]` No CSRF Protection on Any Form

**Files**: All forms (`fillForm.php`, `followUp.php`, `patientDetailsEdit.php`, `index.php`)

**Description**  
Forms submit without a CSRF token. A malicious website can embed a hidden form that auto-submits to `php/insertRecord.php` or `php/deleteRecord.php` while an admin has an active session. The server cannot distinguish a legitimate request from a forged one.

**Fix**  
Generate and validate a per-session CSRF token:

```php
// In session setup (header.php, after session_start):
if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

// In each form:
<input type="hidden" name="csrf_token" value="<?= $_SESSION['csrf_token'] ?>">

// In each handler (insertRecord.php, etc.):
if (!hash_equals($_SESSION['csrf_token'], $_POST['csrf_token'] ?? '')) {
    http_response_code(403);
    exit('Invalid CSRF token');
}
```

---

### WEB-HIGH-02 `[HIGH]` GET Method Used for All Data Mutations

**Files**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`, form `method` attributes in `fillForm.php`, `followUp.php`, etc.

**Description**  
All patient data operations use HTTP GET. Consequences:
- Full patient data (names, diagnoses, medications) is embedded in the URL and persisted in browser history, server logs, proxy logs, and referrer headers.
- GET requests can be triggered by prefetch, link scanners, and the browser's back button — violating HTTP semantics for non-idempotent operations.
- A shared computer exposes patient data in the browser address bar history.

**Fix**  
Change all forms to `method="POST"` and update handlers to use `$_POST` instead of `$_GET`. For delete actions, use a POST form with a hidden `_method=DELETE` field or simply a `?action=delete` POST.

---

### WEB-HIGH-03 `[HIGH]` No Logout Functionality

**Files**: `includes/header.php`, entire Web-App

**Description**  
There is no logout link, button, or endpoint anywhere in the application. Once an admin logs in, their session persists until the PHP session naturally expires (default 24 minutes of inactivity, or the browser is closed). On shared machines, any subsequent user of the same browser has full access to the clinic system.

**Fix**  
Create `logout.php`:

```php
<?php
session_start();
session_unset();
session_destroy();
setcookie(session_name(), '', time() - 42000, '/');
header("Location: index.php?logout=success");
exit();
```

Add a "Logout" link to `includes/header.php` pointing to `logout.php`.

---

### WEB-HIGH-04 `[HIGH]` Raw SQL and Error Details Exposed to Browser

**Files**: `php/insertRecord.php` (lines 55–56), `php/insertFollowUp.php`, `php/updateRecord.php`

**Description**  
On SQL failure, the raw query and database error are echoed directly:

```php
echo "Error: " . $sql . "<br>" . $conn->error;
echo "Record NOT INSERTED";
```

This exposes table names, column names, data types, and query logic to anyone who can cause an error — even accidental ones.

**Fix**  
Log errors server-side; return a generic message to the browser:

```php
if (!mysqli_query($conn, $sql)) {
    error_log("DB error in insertRecord: " . $conn->error);
    header("Location: ../fillForm.php?error=insert_failed");
    exit();
}
```

---

### WEB-HIGH-05 `[HIGH]` Orphaned Follow-up Records on Patient Delete

**File**: `php/deleteRecord.php`

**Description**  
Deleting a patient removes only the `patient_data` row. All associated `follow_up_data` rows referencing that `id` remain in the database as orphaned records that are never displayed, never cleaned up, and inflate the follow-up counts on the dashboard.

**Fix**  
Delete follow-ups first, then the patient, wrapped in a transaction:

```php
$conn->begin_transaction();
try {
    $stmt1 = $conn->prepare("DELETE FROM follow_up_data WHERE id = ?");
    $stmt1->bind_param("i", $id);
    $stmt1->execute();

    $stmt2 = $conn->prepare("DELETE FROM patient_data WHERE id = ?");
    $stmt2->bind_param("i", $id);
    $stmt2->execute();

    $conn->commit();
} catch (Exception $e) {
    $conn->rollback();
    error_log("Delete failed: " . $e->getMessage());
}
```

Alternatively, add `ON DELETE CASCADE` to the `follow_up_data` FK constraint in the schema.

---

## MEDIUM Issues

---

### WEB-MED-01 `[MEDIUM]` Follow-up Number Ordering Breaks After Follow-up 9

**File**: `php/insertFollowUp.php`

**Description**  
`follow_up_num` is a `VARCHAR` column. `MAX()` on VARCHAR is lexicographic:

```
MAX("1","2","3","4","5","6","7","8","9","10") = "9"
```

So the 11th follow-up is assigned number `10` (correct by coincidence), but after `"9"` is returned as MAX, subsequent inserts produce `10`, then `10` again, then `10` again — the counter gets stuck.

**Fix (option A — preferred)**: Change `follow_up_num` to `INT` in the schema and update all PHP/HTML references.

**Fix (option B — no schema change)**:
```php
$sql_max = "SELECT MAX(CAST(follow_up_num AS UNSIGNED)) AS max_num
            FROM follow_up_data WHERE id = ?";
```

---

### WEB-MED-02 `[MEDIUM]` `SUM(paid)` on VARCHAR Column Silently Returns Wrong Earnings

**File**: `includes/earning.php`

**Description**  
`paid` and `balance` columns in both tables are `VARCHAR`. `SUM(paid)` coerces each value to a number; non-numeric strings (e.g., `"500 cash"`, `"N/A"`, empty string) are silently treated as `0`. The earnings totals on the dashboard will be wrong with no indication of the error.

**Fix**  
Change `paid` and `balance` in both `patient_data` and `follow_up_data` to `DECIMAL(10,2)`. Enforce numeric-only input in `fillForm.php` and `followUp.php` with HTML5 `type="number"` and server-side validation:

```php
$paid = filter_var($_POST['paid'], FILTER_VALIDATE_FLOAT);
if ($paid === false) { /* error handling */ }
```

---

### WEB-MED-03 `[MEDIUM]` `session_start()` Called After Potential Output

**File**: `includes/logincheck.php` (line 41)

**Description**  
`session_start()` is called inside a conditional block, deep in the file. If any whitespace, BOM character, or PHP warning is emitted before this line, the call fails silently and the session is not set, breaking login.

**Fix**  
Move `session_start()` to the very first line of `logincheck.php`, before any conditional logic:

```php
<?php
session_start();

if (isset($_POST['login-submit'])) {
    // ... rest of handler
}
```

---

### WEB-MED-04 `[MEDIUM]` No Server-Side Input Validation

**Files**: `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`

**Description**  
No type, length, or format validation is applied to any input. Examples of silent data corruption:
- `age` accepts `"abc"` — stored as `0` in an `INT` column without error.
- `phone` accepts arbitrary text including scripts.
- `paid` accepts `"N/A"` — breaks earnings calculations.
- `id` (in follow-up and delete) accepts non-numeric strings.

**Fix**  
Add server-side validation before every INSERT/UPDATE:

```php
$age = filter_var($_POST['age'], FILTER_VALIDATE_INT, ['options' => ['min_range' => 0, 'max_range' => 150]]);
if ($age === false) {
    header("Location: ../fillForm.php?error=invalid_age");
    exit();
}

$id = filter_var($_POST['id'], FILTER_VALIDATE_INT);
if ($id === false || $id <= 0) {
    http_response_code(400);
    exit('Invalid patient ID');
}
```

---

### WEB-MED-05 `[MEDIUM]` No DB Transaction on Follow-up Insert

**File**: `php/insertFollowUp.php`

**Description**  
The follow-up insert involves two steps: SELECT MAX(follow_up_num), then INSERT with that value + 1. These are not wrapped in a transaction. Under concurrent access (two staff members adding follow-ups to the same patient simultaneously), both could read the same MAX value and insert duplicate follow-up numbers.

**Fix**  
Wrap in a transaction with a table lock or use `SELECT ... FOR UPDATE`:

```php
$conn->begin_transaction();

$stmt_max = $conn->prepare(
    "SELECT MAX(CAST(follow_up_num AS UNSIGNED)) AS max_num FROM follow_up_data WHERE id = ? FOR UPDATE"
);
$stmt_max->bind_param("i", $id);
$stmt_max->execute();
$max_num = $stmt_max->get_result()->fetch_assoc()['max_num'] ?? 0;
$follow_up_num = $max_num + 1;

$stmt_ins = $conn->prepare("INSERT INTO follow_up_data (...) VALUES (?, ..., ?)");
// bind and execute ...
$conn->commit();
```

---

## LOW Issues

---

### WEB-LOW-01 `[LOW]` Legacy and Scratch Files in Production Directory

**Files**: `patientDetailsOLD.php`, `testSideNavForm.php`

**Description**  
`patientDetailsOLD.php` is an old version of the patient details page and `testSideNavForm.php` is a UI prototype. Both are served as live PHP files, expose old (and potentially more vulnerable) code paths, and add confusion to the codebase.

**Fix**: Delete or move to a non-web-accessible location. If version history is needed, Git is the appropriate tool.

---

### WEB-LOW-02 `[LOW]` MySQL Root User With No Password

**File**: `includes/connection.php`

**Description**  
```php
$conn = mysqli_connect("localhost", "root", "", "hospital");
```
The connection uses the MySQL `root` user with an empty password. On a shared or misconfigured host, any MySQL client that can reach localhost can connect as root.

**Fix**  
Create a dedicated MySQL user with minimal privileges for the application:

```sql
CREATE USER 'clinic_app'@'localhost' IDENTIFIED BY 'strong_random_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON hospital.* TO 'clinic_app'@'localhost';
FLUSH PRIVILEGES;
```

Update `connection.php` to use the new credentials. Store credentials in an environment variable or a config file outside the web root.

---

### WEB-LOW-03 `[LOW]` Commented-Out Email Validation in Login Handler

**File**: `includes/logincheck.php` (lines 13–23)

**Description**  
Email format validation and sanitisation are commented out with no explanation:

```php
/*
$mailuid = filter_var($mailuid, FILTER_SANITIZE_EMAIL);
if (!filter_var($mailuid, FILTER_VALIDATE_EMAIL) === true){
    header("Location: ../index.php?error=EmailHasErrors ");
    exit();
}
*/
```

The comment wraps logic that should be active. Without it, the login allows emails with invalid formats through to the database query, which increases the injection surface slightly (though the prepared statement mitigates the injection risk itself).

**Fix**  
Restore the validation and remove the comment wrapper. Adjust the condition — `!filter_var(...) === true` has a precedence bug; correct form is `filter_var(...) === false`:

```php
$mailuid = filter_var($mailuid, FILTER_SANITIZE_EMAIL);
if (filter_var($mailuid, FILTER_VALIDATE_EMAIL) === false) {
    header("Location: ../index.php?error=EmailHasErrors");
    exit();
}
```

---

## Prioritised Fix Roadmap

### Phase 1 — Critical Security (block deployment until resolved)

| # | Issue | File(s) |
|---|---|---|
| 1 | WEB-SEC-01: Convert all queries to prepared statements | `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php`, `php/deleteRecord.php`, `patientDetails.php`, `patientDetailsEdit.php`, `includes/filter.php` |
| 2 | WEB-SEC-02: Hash passwords with bcrypt | `includes/logincheck.php`, DB migration |
| 3 | WEB-SEC-03: Escape all HTML output with `htmlspecialchars` | All `.php` pages that echo DB data |
| 4 | WEB-SEC-04: Add session guard to action endpoints | `php/*.php` |

### Phase 2 — High Impact

| # | Issue | File(s) |
|---|---|---|
| 5 | WEB-HIGH-01: Add CSRF tokens to all forms | All form pages + handlers |
| 6 | WEB-HIGH-02: Switch mutations from GET to POST | `php/*.php` + form pages |
| 7 | WEB-HIGH-03: Implement logout | New `logout.php` + `includes/header.php` |
| 8 | WEB-HIGH-04: Hide DB errors from browser | `php/*.php` |
| 9 | WEB-HIGH-05: Cascade delete follow-ups on patient delete | `php/deleteRecord.php` |

### Phase 3 — Data Integrity

| # | Issue | File(s) |
|---|---|---|
| 10 | WEB-MED-01: Fix follow-up number MAX on VARCHAR | `php/insertFollowUp.php`, schema |
| 11 | WEB-MED-02: Change `paid`/`balance` to DECIMAL | Schema migration + all form handlers |
| 12 | WEB-MED-04: Add server-side input validation | `php/insertRecord.php`, `php/insertFollowUp.php`, `php/updateRecord.php` |
| 13 | WEB-MED-05: Wrap follow-up insert in transaction | `php/insertFollowUp.php` |

### Phase 4 — Housekeeping

| # | Issue | File(s) |
|---|---|---|
| 14 | WEB-MED-03: Move `session_start()` to top | `includes/logincheck.php` |
| 15 | WEB-LOW-01: Remove legacy/scratch files | `patientDetailsOLD.php`, `testSideNavForm.php` |
| 16 | WEB-LOW-02: Replace root DB user | `includes/connection.php` |
| 17 | WEB-LOW-03: Restore and fix email validation | `includes/logincheck.php` |
