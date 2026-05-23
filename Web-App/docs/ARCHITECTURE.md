# Architecture — Web-App (Mahajan Homeo Clinic)

## 1. Overview

The Web-App is a server-rendered PHP + MySQL administration interface for managing homeopathic patient records. It provides patient intake, follow-up tracking, and a statistics dashboard. It is designed for desktop-browser use and serves as the primary data entry point for clinic staff.

- **Backend**: PHP (no framework), MySQLi procedural API  
- **Frontend**: Bootstrap 4.3.1 + Material Design Bootstrap (MDB), jQuery 3.4.1  
- **Database**: MySQL, database name `hospital`  
- **Voice input**: Annyang + SpeechKITT (experimental)  
- **Hosting**: Free-tier (epizy.com); demo at `http://hospital.epizy.com/?i=1`

---

## 2. Request / Response Flow

All pages are server-rendered. There is no REST API, AJAX layer, or SPA framework.

```
Browser → GET/POST → PHP page → MySQLi → MySQL DB → PHP renders HTML → Browser
```

Authentication is enforced by `includes/header.php`, which starts the session and redirects to `index.php` if `$_SESSION['adminId']` is not set.

### Login Flow
```
index.php  ──POST──►  includes/logincheck.php
                              │
                    ┌─────────▼──────────┐
                    │ SELECT admin_users  │
                    │ WHERE email OR uid  │
                    └─────────┬──────────┘
                              │ match + password check
                    ┌─────────▼──────────┐
                    │ session_start()     │
                    │ set adminId, etc.   │
                    └─────────┬──────────┘
                              │
                         home.php
```

### Patient Add Flow
```
fillForm.php  ──GET──►  php/insertRecord.php  ──►  fillForm.php?insert=success
```

### Follow-up Add Flow
```
patientDetails.php  ──►  followUp.php  ──GET──►  php/insertFollowUp.php
                                                         │
                                              auto-increment follow_up_num
                                                         │
                                              patientDetails.php?insert=success
```

---

## 3. Directory Structure

```
Web-App/
├── docs/                     # Project documentation (this directory)
├── index.php                 # Login page
├── home.php                  # Post-login landing / hub
├── dashboard.php             # Statistics and summary
├── records.php               # Paginated patient list
├── fillForm.php              # Add new patient (full intake form)
├── patientDetails.php        # View patient + follow-ups accordion
├── patientDetailsEdit.php    # Edit patient details
├── patientDetailsOLD.php     # Unused legacy version
├── followUp.php              # Add follow-up form
├── detailedRecords.php       # Extended records view
├── search.php                # Search results
├── updateRecord.php          # Edit patient entry point
├── testSideNavForm.php       # UI prototype / scratch file
├── includes/
│   ├── connection.php        # MySQLi connection
│   ├── header.php            # Session guard + Bootstrap navbar
│   ├── footer.php            # Page footer
│   ├── logincheck.php        # POST login handler
│   ├── earning.php           # Aggregated count / earnings SQL
│   ├── filter.php            # Sort + pagination SQL builder
│   ├── clock.php             # Real-time clock widget
│   ├── recordCard.php        # Patient card template
│   ├── patientDetailsForm.php # Patient details form partial
│   ├── detailsCard.php       # Details card component
│   ├── fillFormSideNav.php   # Side nav for fill form
│   └── followUpForm.php      # Follow-up display partial
├── php/
│   ├── insertRecord.php      # INSERT new patient (GET params)
│   ├── insertFollowUp.php    # INSERT follow-up (GET params)
│   ├── updateRecord.php      # UPDATE patient (GET params)
│   └── deleteRecord.php      # DELETE patient
├── css/
│   └── style.css             # Custom overrides (minimal)
├── js/
│   └── script.js             # Voice input (Annyang + SpeechKITT)
├── img/                      # Static images
├── setup/                    # DB setup scripts
└── Screenshots/              # UI screenshots
```

---

## 4. Database Schema

### `admin_users`
```sql
id_admin       INT         PRIMARY KEY AUTO_INCREMENT
uid_admin      TINYTEXT                              -- username / uid
email_admin    TINYTEXT                              -- login email
password_admin LONGTEXT                              -- plaintext password (critical issue)
dateOfBirth    DATE
degree         TEXT
firstName      TEXT
lastName       TEXT
```

### `patient_data`
```sql
id             INT         PRIMARY KEY AUTO_INCREMENT
firstName      VARCHAR
middleName     VARCHAR
lastName       VARCHAR
age            INT
sex            VARCHAR
occupation     VARCHAR
address        TEXT
phone          VARCHAR
regno          VARCHAR                               -- registration number
height         INT
weight         INT
diagnosis      VARCHAR
cc1            TEXT                                  -- Chief Complaint 1
cc2            TEXT
cc3            TEXT
appetite       TEXT                                  -- Homeopathic history
desire         TEXT
aversions      TEXT
thirst         TEXT
perspiration   TEXT
sleep          TEXT
stool          TEXT
urine          TEXT
menses         TEXT
thermal        TEXT
mind           TEXT
hobbies        TEXT
particulars    TEXT
on_examination TEXT
path_inv       TEXT
previous_rx    TEXT
past_history   TEXT
family_history TEXT
treatment      TEXT
paid           VARCHAR                               -- stored as string; should be DECIMAL
balance        VARCHAR                               -- stored as string; should be DECIMAL
followUp1      VARCHAR                               -- legacy; unused
followUp2      VARCHAR
followUp3      VARCHAR
followUp4      VARCHAR
dateJoined     DATETIME    DEFAULT CURRENT_TIMESTAMP
```

### `follow_up_data`
```sql
followUpId          INT      PRIMARY KEY AUTO_INCREMENT
id                  INT      (FK → patient_data.id — not enforced at DB level)
date                DATETIME DEFAULT CURRENT_TIMESTAMP
regno               VARCHAR
weight              VARCHAR                          -- should be INT/DECIMAL
treatment_output    VARCHAR
other_complains     TEXT
treatment           TEXT
medicine_duration   VARCHAR
paid                VARCHAR                          -- should be DECIMAL
balance             VARCHAR                          -- should be DECIMAL
follow_up_num       VARCHAR                          -- should be INT; causes MAX() ordering bug
```

---

## 5. Authentication

- Session variables set on login: `adminId`, `adminUid`, `firstName`, `lastName`
- Session guard in `includes/header.php`: if no `$_SESSION['adminId']`, redirect to `index.php`
- Password comparison: **plaintext** — no `password_hash()` / `password_verify()` used
- No logout endpoint exists anywhere in the codebase
- No CSRF protection on any form

---

## 6. Key Page Responsibilities

| Page | Method | Auth Guard | Database Operation |
|---|---|---|---|
| `index.php` | GET | None | None |
| `includes/logincheck.php` | POST | None | SELECT admin_users |
| `home.php` | GET | header.php | None |
| `dashboard.php` | GET | header.php | includes/earning.php aggregates |
| `records.php` | GET | header.php | SELECT patient_data (paginated) |
| `fillForm.php` | GET | header.php | None (form only) |
| `php/insertRecord.php` | GET | None | INSERT patient_data |
| `patientDetails.php` | GET | header.php | SELECT patient + follow_ups |
| `patientDetailsEdit.php` | GET | header.php | SELECT patient |
| `php/updateRecord.php` | GET | None | UPDATE patient_data |
| `followUp.php` | GET | header.php | SELECT patient (prefill) |
| `php/insertFollowUp.php` | GET | None | SELECT MAX + INSERT follow_up_data |
| `php/deleteRecord.php` | GET | None | DELETE patient_data |
| `search.php` | POST | header.php | SELECT patient_data WHERE LIKE |

> **Note**: `php/` action handlers have no auth guard — they can be called directly without a session.

---

## 7. Frontend Architecture

- All pages use server-side rendering (no JS templating).
- `includes/header.php` is included on every authenticated page and provides the Bootstrap navbar and session check.
- `includes/footer.php` closes the page.
- Bootstrap 4 grid provides responsive layout.
- Voice input (`js/script.js`) uses Annyang for speech recognition and SpeechKITT for the visual UI. Commands are wired to form fields in `fillForm.php`.

---

## 8. Earning / Statistics Calculation

`includes/earning.php` is included in `dashboard.php`. It executes 12 queries to compute:

| Period | New Patients | New Follow-ups | Total Earnings |
|---|---|---|---|
| Today | COUNT | COUNT | SUM(paid) from both tables |
| Last 7 days | COUNT | COUNT | SUM(paid) |
| Last 30 days | COUNT | COUNT | SUM(paid) |
| Last 365 days | COUNT | COUNT | SUM(paid) |

The 12 queries expose 16 PHP variables (`$todayNewCount`, `$todayFollowCount`, `$todayEarnCount`, etc.) used in `dashboard.php`.

---

## 10. Next.js Web App (WebPatientRecords/)

A modern parallel web app at `C:\xampp\htdocs\Hospital-Data-Web-App\WebPatientRecords\` sharing the same `hospital` MySQL database.

### Stack
- **Framework**: Next.js 14 App Router (TypeScript strict)
- **UI**: shadcn/ui (Radix primitives) + Tailwind CSS v3 with HSL CSS variables
- **Theme**: next-themes; `darkMode: "class"`; clinic-blue (`hsl(221 83% 53%)`) primary
- **Auth**: iron-session v8; encrypted cookie; `middleware.ts` route guard
- **DB**: mysql2 promise pool; parameterised `pool.execute()` throughout
- **Forms**: react-hook-form + zod (`zodResolver`)
- **Validation**: zod schemas in `lib/validations.ts`

### Route Structure
```
app/
├── (auth)/login/          # Login page (no session guard)
├── (dashboard)/           # Session-guarded layout with sidebar + navbar
│   ├── dashboard/         # Stats + recent tables
│   ├── patients/          # List (sortable, paginated)
│   ├── patients/new/      # Add patient form
│   ├── patients/[id]/     # Patient detail + follow-up accordion
│   ├── patients/[id]/edit/
│   ├── patients/[id]/followup/
│   └── records/           # Wide records table + CSV export
└── api/
    ├── auth/login|logout
    ├── dashboard
    ├── search
    ├── patients            # GET (list) + POST (create)
    ├── patients/[id]       # GET + PUT + DELETE
    └── patients/[id]/followups  # GET + POST
```

### Key Constraints
- `ExecuteValues` (mysql2) excludes `undefined` — all optional fields null-coerced with `?? null` before `pool.execute()`
- `z.coerce.number()` resolver input type is `unknown` — cast to `as any` on `useForm<PatientInput>` resolver
- Sort column injection prevented by `SORT_WHITELIST` array; `ORDER BY` uses whitelisted identifier only

---

## 9. Known Architectural Constraints (PHP App)

- **No separation of concerns**: SQL queries are embedded directly in page files and includes. There is no model, controller, or service layer.
- **GET for writes**: All data mutations (`insertRecord`, `updateRecord`, `insertFollowUp`, `deleteRecord`) use HTTP GET instead of POST. This means browser history and server logs capture sensitive patient data, and the operations can be triggered by simply following a link.
- **No transactions**: Multi-step operations (e.g., counting max follow_up_num then inserting) are not wrapped in a DB transaction, opening a race condition window.
- **No data isolation**: All admin accounts share a single MySQL database with no row-level ownership or multi-tenancy.
