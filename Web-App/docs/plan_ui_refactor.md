# UI Refactor Plan — Mahajan Homeo Clinic Web App

**Date**: 2026-05-23  
**Approach**: Full React (Next.js) frontend + Next.js API routes, retiring PHP. MySQL stays.  
**Component source**: [21st.dev](https://21st.dev) (React + Tailwind CSS, Next.js-ready)  
**Theming**: Light / Dark mode via `next-themes` + Tailwind CSS variables  
**New project location**: `C:\xampp\htdocs\Hospital-Data-Web-App\WebPatientRecords`

---

## 1. Overview

The current app is a PHP-rendered Bootstrap site. This refactor replaces it entirely with a **Next.js 14 App Router** application that:

- Uses **21st.dev** components for every major UI surface
- Exposes all data operations as **Next.js API routes** (replacing PHP handlers)
- Connects directly to the existing **MySQL `hospital` database** via `mysql2`
- Supports **light and dark themes** with a toggle in the navbar, persisted to localStorage
- Is fully **responsive** (mobile → desktop) via Tailwind CSS
- **Synchronises with Firebase Realtime Database** to share data with the Android app

The XAMPP MySQL server continues running. XAMPP's PHP/Apache is no longer needed once the Next.js app is running.

### Data Flow Summary

```
Android App (Room/SQLite)  ←──→  Firebase Realtime Database  ←──→  Next.js App (MySQL)
     offline-first                    single source of sync             offline-first
```

Both apps write to their local DB first, then sync to Firebase when online. Neither app writes directly to the other's local DB. Firebase is the sync bus, not the primary store.

---

## 2. Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Framework | Next.js 14 (App Router) | Modern server components + API routes in one project |
| Language | TypeScript | Type safety, better IDE support |
| Styling | Tailwind CSS v3 | Required by 21st.dev components |
| Component base | shadcn/ui | Foundation that 21st.dev builds on; provides primitives (Dialog, Sheet, Table, Input, etc.) |
| 21st.dev components | Copy-paste via `npx shadcn@latest add "https://21st.dev/r/..."` | Navbar, sidebar, dashboard cards, tables, forms, login |
| Animations | Magic UI (via 21st.dev) | Blur Fade, Border Beam, Text Reveal, Animated Shiny Text |
| Theme | `next-themes` | Dark/light toggle with system default + localStorage persistence |
| Database | `mysql2` (promise API) | Direct MySQL queries; mirrors existing PHP logic |
| Auth | `iron-session` | Lightweight encrypted server-side sessions; replaces PHP `$_SESSION` |
| Icons | `lucide-react` | Already used by shadcn/ui; consistent icon set |
| Forms | `react-hook-form` + `zod` | Validation to replace missing PHP server-side validation |
| Firebase | `firebase-admin` (Node.js SDK) | Server-side read/write to Firebase Realtime Database from API routes |
| IDs | `uuid` (v4) | Generate UUID keys for Firebase to prevent Android ↔ Web ID collisions |

---

## 3. Project Structure

```
WebPatientRecords/                   ← new Next.js project
├── app/
│   ├── (auth)/
│   │   └── login/
│   │       └── page.tsx             ← Login page
│   ├── (dashboard)/
│   │   ├── layout.tsx               ← Shared layout: sidebar + navbar
│   │   ├── dashboard/
│   │   │   └── page.tsx             ← Stats dashboard
│   │   ├── patients/
│   │   │   ├── page.tsx             ← Patient records list + search
│   │   │   ├── new/
│   │   │   │   └── page.tsx         ← Add patient form
│   │   │   └── [id]/
│   │   │       ├── page.tsx         ← Patient details + follow-ups
│   │   │       ├── edit/
│   │   │       │   └── page.tsx     ← Edit patient
│   │   │       └── followup/
│   │   │           └── page.tsx     ← Add follow-up
│   │   └── records/
│   │       └── page.tsx             ← Detailed records view
│   ├── api/
│   │   ├── auth/
│   │   │   ├── login/route.ts
│   │   │   └── logout/route.ts
│   │   ├── patients/
│   │   │   ├── route.ts             ← GET (list) + POST (create)
│   │   │   └── [id]/
│   │   │       ├── route.ts         ← GET + PUT + DELETE
│   │   │       └── followups/
│   │   │           └── route.ts     ← GET + POST
│   │   ├── dashboard/
│   │   │   └── route.ts             ← Stats aggregates
│   │   ├── search/
│   │   │   └── route.ts             ← Search patients
│   │   └── sync/
│   │       ├── push/route.ts        ← Push local changes to Firebase
│   │       ├── pull/route.ts        ← Pull Firebase changes into MySQL
│   │       ├── status/route.ts      ← Sync status + pending queue count
│   │       └── full/route.ts        ← Full re-sync (manual trigger)
│   ├── globals.css                  ← Tailwind base + CSS variables (light/dark)
│   └── layout.tsx                   ← Root layout with ThemeProvider
├── components/
│   ├── ui/                          ← shadcn/ui primitives (auto-generated)
│   ├── layout/
│   │   ├── app-navbar.tsx           ← 21st.dev navbar component
│   │   ├── app-sidebar.tsx          ← 21st.dev sidebar component
│   │   └── theme-toggle.tsx         ← Light/dark toggle button
│   ├── dashboard/
│   │   ├── stat-card.tsx            ← 21st.dev feature/stats card
│   │   ├── sync-status-badge.tsx    ← Online/offline sync indicator
│   │   └── recent-patients-table.tsx
│   ├── patients/
│   │   ├── patient-table.tsx        ← 21st.dev table component
│   │   ├── patient-form.tsx         ← 21st.dev form component (add/edit)
│   │   └── followup-form.tsx        ← 21st.dev form component
│   └── shared/
│       ├── delete-confirm-dialog.tsx ← shadcn/ui AlertDialog
│       ├── search-bar.tsx
│       └── loading-skeleton.tsx     ← Magic UI Blur Fade wrapper
├── lib/
│   ├── db.ts                        ← mysql2 connection pool
│   ├── session.ts                   ← iron-session config + helpers
│   ├── auth.ts                      ← login/logout logic + bcrypt
│   ├── firebase.ts                  ← firebase-admin initialisation
│   ├── sync.ts                      ← sync logic (push/pull/conflict)
│   └── validations.ts               ← zod schemas for all forms
└── types/
    └── index.ts                     ← Patient, FollowUp, Admin, FirebasePatient interfaces
```

---

## 4. Pages & 21st.dev Component Mapping

### 4.1 Login Page (`/login`)

**21st.dev source**: Login components (25 available at `21st.dev/s/login`)

**Design**:
- Two-column layout: left panel with clinic branding + Magic UI **Animated Shiny Text** for the clinic name, right panel with the login form card
- Dark panel on the left (stays dark even in light mode — branding anchor)
- shadcn/ui `Card` + `Input` + `Button` for the form
- Error messages inline under fields via `react-hook-form`
- Theme toggle in top-right corner so user can switch before logging in

**Fields**: Email or username, Password, Submit button  
**Validation (zod)**: Both fields required; redirects to `/dashboard` on success

---

### 4.2 App Shell — Navbar + Sidebar

**21st.dev source**: Navbar components (43 available at `21st.dev/s/navbar`), Sidebar components (14 available at `21st.dev/s/sidebar`)

#### Navbar
- Fixed top bar with: clinic logo/name on left, search bar in centre, theme toggle + logout button on right
- Magic UI **Border Beam** accent on the navbar bottom edge (subtle animated gradient border)
- On mobile: hamburger menu that opens the sidebar as a Sheet (shadcn/ui)
- Search bar triggers a command palette (`cmdk`) that searches patients by name/regno in real time
- **Sync status indicator**: small dot (green = synced, amber = pending, red = offline) in the top bar

#### Sidebar (desktop)
- Collapsible left sidebar (icon-only collapsed, full-width expanded)
- Nav items: Dashboard, Patients, Add Patient, Detailed Records
- Active item highlighted with a pill indicator
- Doctor name + avatar at the bottom
- `next-themes` toggle integrated at the bottom of the sidebar too

---

### 4.3 Dashboard (`/dashboard`)

**21st.dev sources**: Dashboard components (30 available at `21st.dev/s/dashboard`), Features section components for stat cards

**Design**:
- Welcome banner: "Good morning, Dr. [Name]" with Magic UI **Text Reveal** animation
- **4 stat cards** (Today / Week / Month / Year), each card showing:
  - New patients count
  - Follow-ups count
  - Earnings total
  - Cards use 21st.dev Feature cards with icons, animated number counters
  - Magic UI **Shine Border** on hover for each card
- **Recent patients table** (last 7 days) — 21st.dev table component, columns: Name, Date, Diagnosis, Paid, Action
- **Recent follow-ups table** (last 7 days) — same table style
- Personal details section: DOB, Degree, Email in a compact card
- **Sync status card**: shows last sync time, pending changes count, manual "Sync Now" button

**Data fetching**: Server Component calls `/api/dashboard` which returns all aggregate counts in one response.

---

### 4.4 Patient Records (`/patients`)

**21st.dev sources**: Table components (36 available at `21st.dev/s/table`), Navbar/search components

**Design**:
- Full-width data table with:
  - Columns: Reg No, Name, Date Joined, Phone, Diagnosis, Paid, Balance, Actions
  - Actions per row: View Details, Add Follow-up, Delete (with confirm dialog)
  - Column header click → sort (firstName, lastName, dateJoined, regno, diagnosis)
  - Pagination (20 per page) with prev/next controls
- Inline search bar at the top filters by name/regno as you type (debounced, hits `/api/search`)
- Sort filter buttons styled as pill tabs (replacing Bootstrap dropdown buttons)
- Delete uses shadcn/ui **AlertDialog** with confirm/cancel — no browser `confirm()`
- Magic UI **Blur Fade** stagger animation on initial table row load

---

### 4.5 Add Patient (`/patients/new`)

**21st.dev sources**: Form components (40 available at `21st.dev/s/form`)

**Design**:
- Sticky left sidebar nav matching the original `fillFormSideNav.php` sections
- Multi-section form laid out in a two-column grid on desktop, single column on mobile
- **Sections** (matching original fields, styled with shadcn/ui `Separator` and section headers):
  1. Personal — First, Middle, Last name, Age, Sex (Select), Occupation
  2. Contact — Address, Phone, Registration Number
  3. Vitals — Height, Weight, Clinical Diagnosis
  4. Chief Complaints — CC1, CC2, CC3
  5. Homeopathic History — Appetite, Desire, Aversions, Thirst, Perspiration, Sleep, Stool, Urine, Menses, Thermal, Mind (all `<Textarea>`)
  6. Case Particulars — Hobbies, Particulars, On Examination, Path Inv, Previous Rx, Past History, Family History
  7. Treatment — Treatment (Textarea), Paid, Balance
- Voice input (Annyang) preserved as a floating mic button that activates field-fill by voice
- **Sticky submit bar** at the bottom that appears when form is dirty
- Real-time validation feedback via `react-hook-form` + `zod`
- Success toast notification (shadcn/ui `Sonner`) on submit

---

### 4.6 Patient Details (`/patients/[id]`)

**Design**:
- Top card: Patient name, Reg No, Date Joined — with Edit and Delete action buttons
- **Follow-ups accordion**: each follow-up is a collapsible card (shadcn/ui `Accordion`)
  - Sorted by follow-up number descending
  - Each card shows: date, follow-up #, weight, treatment, paid/balance
  - "Add Follow-up" button in the header
- **Initial Details accordion**: collapsible section showing all intake fields (read-only)
  - Uses the same two-column grid as the form, but all fields are read-only
- Magic UI **Blur Fade** entrance animation on the follow-up cards

---

### 4.7 Edit Patient (`/patients/[id]/edit`)

- Same form layout as Add Patient (4.5), pre-filled with existing data
- shadcn/ui `Skeleton` loading state while data fetches
- Unsaved changes warning on navigation away (matching original `beforeunload` behavior)

---

### 4.8 Add Follow-up (`/patients/[id]/followup`)

**21st.dev sources**: Form components

**Design**:
- Compact form (much smaller than full patient form)
- Read-only patient info card at top (Name, RegNo, Phone, ID)
- Editable fields: Weight, Treatment Output (Select), Other Complaints (Textarea), Treatment (Textarea), Medicine Duration (Select), Paid, Balance
- Submits to `/api/patients/[id]/followups` via POST
- Redirects to patient details on success

---

### 4.9 Detailed Records (`/records`)

- Same table component as Patient Records but with extended columns
- Additional columns: Address, all Chief Complaints visible
- Exportable to CSV (browser-side using `papaparse`)

---

## 5. API Routes (replacing PHP handlers)

All routes check `iron-session` for `adminId` before processing. Unauthenticated requests return `{ error: "Unauthorized" }` with HTTP 401.

```
POST   /api/auth/login              ← replaces includes/logincheck.php
POST   /api/auth/logout             ← replaces logout.php

GET    /api/patients                ← replaces filter.php + records.php (list, sort, paginate)
POST   /api/patients                ← replaces php/insertRecord.php
GET    /api/patients/[id]           ← replaces patientDetails.php SELECT
PUT    /api/patients/[id]           ← replaces php/updateRecord.php
DELETE /api/patients/[id]           ← replaces php/deleteRecord.php (with cascade)

GET    /api/patients/[id]/followups ← replaces follow-up SELECT in patientDetails.php
POST   /api/patients/[id]/followups ← replaces php/insertFollowUp.php

GET    /api/dashboard               ← replaces includes/earning.php (all 12 aggregates in one call)
GET    /api/search?q=               ← replaces search.php

POST   /api/sync/push               ← push pending MySQL changes to Firebase
POST   /api/sync/pull               ← pull Firebase changes into MySQL (last-write-wins)
GET    /api/sync/status             ← { lastSync, pendingCount, isOnline }
POST   /api/sync/full               ← full re-sync: push all MySQL records to Firebase
```

### Validation
Every `POST` and `PUT` route validates the request body against a **zod schema** before touching the database. Invalid payloads return `{ error: "...", fields: {...} }` with HTTP 422.

### Error handling
All routes wrap DB calls in `try/catch`. Errors are logged server-side; clients receive a generic `{ error: "Internal server error" }` with HTTP 500. No SQL or stack traces are ever sent to the client.

### Write-Through Sync
Patient create/update/delete routes write to MySQL **and** immediately attempt to push to Firebase. If Firebase is unreachable (offline), the change is recorded in the `sync_queue` table for later delivery. This keeps the local MySQL DB as the source of truth while Firebase stays as up-to-date as the network allows.

---

## 6. Database Layer (`lib/db.ts`)

```typescript
// Single connection pool shared across all API routes
import mysql from 'mysql2/promise';

const pool = mysql.createPool({
  host: 'localhost',
  user: 'root',
  password: '',
  database: 'hospital',
  waitForConnections: true,
  connectionLimit: 10,
});

export default pool;
```

All queries use **parameterised prepared statements**:
```typescript
const [rows] = await pool.execute(
  'SELECT * FROM patient_data WHERE id = ?',
  [id]
);
```

---

## 7. Authentication (`lib/session.ts`, `lib/auth.ts`)

**iron-session** stores an encrypted session cookie (replacing PHP `$_SESSION`):

```typescript
// Session shape
interface SessionData {
  adminId: number;
  adminUid: string;
  firstName: string;
  lastName: string;
}
```

- **Login**: `bcrypt.compare(password, hash)` — same logic as the fixed PHP version
- **Plaintext migration**: same transparent re-hash on first login
- **Session cookie**: `HttpOnly`, `SameSite: Lax`, `Secure: false` (local dev), 24-hour TTL
- **Middleware** (`middleware.ts`): protects all `/(dashboard)` routes and `/api/**` (except `/api/auth/*`); redirects unauthenticated requests to `/login`

---

## 8. Light / Dark Theme

### Implementation

**`next-themes`** wraps the root layout. The active theme class (`dark`) is applied to `<html>` by next-themes.

**Tailwind CSS** is configured with `darkMode: 'class'` so all `dark:` utility variants activate when `<html class="dark">` is set.

**CSS variables** in `globals.css` define all colours in HSL:

```css
:root {
  --background: 0 0% 100%;
  --foreground: 222 84% 5%;
  --card: 0 0% 100%;
  --card-foreground: 222 84% 5%;
  --primary: 221 83% 53%;        /* clinic blue */
  --primary-foreground: 210 40% 98%;
  --muted: 210 40% 96%;
  --muted-foreground: 215 16% 47%;
  --border: 214 32% 91%;
  --ring: 221 83% 53%;
}

.dark {
  --background: 222 84% 5%;
  --foreground: 210 40% 98%;
  --card: 222 47% 11%;
  --card-foreground: 210 40% 98%;
  --primary: 217 91% 60%;
  --primary-foreground: 222 84% 5%;
  --muted: 217 33% 17%;
  --muted-foreground: 215 20% 65%;
  --border: 217 33% 17%;
  --ring: 224 72% 57%;
}
```

All 21st.dev and shadcn/ui components reference these variables, so the entire app switches theme in one class toggle.

### Theme Toggle Component

A sun/moon icon button in the navbar and sidebar (both locations):

```tsx
// components/layout/theme-toggle.tsx
import { Moon, Sun } from 'lucide-react';
import { useTheme } from 'next-themes';

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();
  return (
    <button onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>
      <Sun className="dark:hidden" />
      <Moon className="hidden dark:block" />
    </button>
  );
}
```

**Persistence**: `next-themes` persists the chosen theme to `localStorage` automatically and reads `prefers-color-scheme` on first visit.

---

## 9. Magic UI Animations (from 21st.dev)

Used tastefully — animations serve purpose, not decoration:

| Component | Where used | Effect |
|---|---|---|
| **Text Reveal** | Dashboard welcome heading | Text fades in word-by-word on load |
| **Blur Fade** | Table rows on initial load | Rows stagger-fade in from slightly blurred |
| **Blur Fade** | Page transitions | Soft fade between route changes |
| **Border Beam** | Stat cards on dashboard | Animated gradient border traces the card edge |
| **Shine Border** | Login card | Subtle shine effect on the login form card |
| **Animated Shiny Text** | Clinic name in the login left panel | Shimmering text for branding |
| **Smooth Cursor** | Global (optional) | Custom cursor for desktop polish |

All animations respect `prefers-reduced-motion` via a wrapper that disables animations when the OS accessibility setting is on.

---

## 10. Responsive Design

| Breakpoint | Layout |
|---|---|
| `< 768px` (mobile) | No sidebar; hamburger opens a bottom sheet; forms single-column; table scrolls horizontally |
| `768px–1024px` (tablet) | Sidebar icon-only (collapsed); forms two-column; full table visible |
| `> 1024px` (desktop) | Full sidebar; forms two-column; dashboard four stat cards in a row |

All 21st.dev components are Tailwind-based and respond to these breakpoints natively.

---

## 11. Cross-Platform Data Contract

Both the web app (MySQL) and the Android app (Room/SQLite) must agree on a shared data shape before either can safely sync via Firebase Realtime Database. This section defines that contract.

### 11.1 Why the Current Sync Is Broken

Two bugs exist in the current Android `FirebaseSyncManager.kt`:

1. **ID collision**: Android SQLite and MySQL each generate their own auto-increment integers independently. When both create a patient, they may both produce `id = 1`. Firebase uses this integer as the key — `/patients/1` — causing silent overwrites. The web patient and the Android patient are two different people.

2. **Edit loss**: The sync compares records using `dateJoined` (patients) and `date` (follow-ups). These fields are set once at creation and never change. An edit on either platform changes other fields but not `dateJoined`, so the sync sees the timestamps as equal and treats both versions as "not changed". The edited fields are silently dropped.

### 11.2 Shared Patient Fields (Firebase path: `/patients/{firebaseId}`)

```json
{
  "firebaseId": "550e8400-e29b-41d4-a716-446655440000",
  "localWebId": 7,
  "localAndroidId": 3,
  "updatedAt": 1716480000000,
  "deleted": false,
  "deletedAt": null,

  "firstName": "Ramesh",
  "lastName": "Patil",
  "middleName": "",
  "age": 45,
  "sex": "Male",
  "occupation": "Farmer",
  "address": "123 Main St",
  "phone": "9876543210",
  "regno": "MH-2024-0012",
  "dateJoined": "2024-03-15",
  "clinicalDiagnosis": "...",
  "height": "170",
  "weight": "68",
  "paid": "500",
  "balance": "0",
  "treatment": "...",
  "cc1": "...", "cc2": "...", "cc3": "...",
  "appetite": "...", "desire": "...", "aversions": "...",
  "thirst": "...", "perspiration": "...", "sleep": "...",
  "stool": "...", "urine": "...", "menses": "...",
  "thermal": "...", "mind": "...",
  "hobbies": "...", "particulars": "...", "onExamination": "...",
  "pathInv": "...", "previousRx": "...", "pastHistory": "...",
  "familyHistory": "..."
}
```

### 11.3 Shared Follow-Up Fields (Firebase path: `/patient_follow_ups/{firebaseId}`)

```json
{
  "firebaseId": "a1b2c3d4-...",
  "patientFirebaseId": "550e8400-...",
  "localWebId": 12,
  "localAndroidId": 5,
  "followUpNum": 3,
  "date": "2024-04-10",
  "updatedAt": 1716480000000,
  "deleted": false,
  "deletedAt": null,

  "weight": "70",
  "treatmentOutput": "Better",
  "otherComplaints": "...",
  "treatment": "...",
  "medicineDuration": "1 month",
  "paid": "200",
  "balance": "0"
}
```

### 11.4 Key Design Decisions

| Decision | Rationale |
|---|---|
| UUID (`firebaseId`) as Firebase key | Eliminates ID collision between Android and web auto-increment integers |
| `updatedAt` as Unix milliseconds | Enables true last-write-wins: the record with the higher `updatedAt` wins |
| `localWebId` + `localAndroidId` stored in Firebase | Each platform looks up its own local ID when applying a pulled record |
| `deleted: true` soft-delete | Hard deletes can't propagate to the other platform; soft deletes can |
| `deletedAt` timestamp | Allows the receiving platform to confirm the delete is newer than its local copy |

---

## 12. Firebase Realtime Database Structure

**Project**: `androidapp-70662`  
**Database URL**: `https://androidapp-70662-default-rtdb.firebaseio.com`

```
/
├── patients/
│   └── {firebaseId}/           ← UUID string key
│       └── { patient fields }
│
└── patient_follow_ups/
    └── {firebaseId}/           ← UUID string key
        └── { follow-up fields }
```

**No Firestore** — stays on Realtime Database to match the existing Android integration.

### Security Rules (for local-only use)

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

For the initial local-only deployment, Firebase Auth is not yet in use. If the laptop is the only device accessing Firebase (via the `firebase-admin` SDK with a service account), the rules can remain `true` for both. Tighten before any cloud exposure.

---

## 13. MySQL Schema Migration

Before building the sync layer, the MySQL `hospital` database needs a small migration to add sync fields. This migration is backward-compatible with the current PHP app running in parallel.

### 13.1 `patient_data` table additions

```sql
ALTER TABLE patient_data
  ADD COLUMN firebaseId VARCHAR(36) UNIQUE DEFAULT NULL,
  ADD COLUMN updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX idx_patient_firebase ON patient_data (firebaseId);
CREATE INDEX idx_patient_updated  ON patient_data (updatedAt);
```

### 13.2 `follow_up_data` table additions

```sql
ALTER TABLE follow_up_data
  ADD COLUMN firebaseId VARCHAR(36) UNIQUE DEFAULT NULL,
  ADD COLUMN patientFirebaseId VARCHAR(36) DEFAULT NULL,
  ADD COLUMN updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX idx_followup_firebase ON follow_up_data (firebaseId);
CREATE INDEX idx_followup_updated  ON follow_up_data (updatedAt);
```

### 13.3 `sync_queue` table (new — for offline write buffering)

```sql
CREATE TABLE sync_queue (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  entityType  ENUM('patient', 'followup') NOT NULL,
  firebaseId  VARCHAR(36) NOT NULL,
  operation   ENUM('upsert', 'delete') NOT NULL,
  payload     JSON NOT NULL,
  createdAt   DATETIME DEFAULT CURRENT_TIMESTAMP,
  attempts    INT DEFAULT 0
);
```

### 13.4 Optional future improvements (separate migration)

These are type-safety improvements that do not affect the sync logic — defer until after the sync layer is stable:

- Change `follow_up_num` from `VARCHAR` to `INT`
- Change `paid` and `balance` columns in both tables from `VARCHAR` to `DECIMAL(10,2)`
- Change `height` and `weight` in both tables from `VARCHAR` to `DECIMAL(5,1)`
- Remove the unused `followUp1`, `followUp2`, `followUp3`, `followUp4` columns from `patient_data`

---

## 14. Sync Architecture (Web App)

### 14.1 Write-Through (online path)

On every patient/follow-up create, update, or delete, the API route:

1. Writes to MySQL (primary store)
2. Generates a UUID `firebaseId` if the record doesn't already have one
3. Constructs the Firebase payload (shared data contract shape from §11)
4. Calls `firebase-admin` to write `/patients/{firebaseId}` or `/patient_follow_ups/{firebaseId}`
5. If Firebase write succeeds → done
6. If Firebase write fails (offline) → inserts into `sync_queue` table

```typescript
// lib/sync.ts — simplified write-through
export async function pushToFirebase(
  entityType: 'patient' | 'followup',
  firebaseId: string,
  payload: FirebasePatient | FirebaseFollowUp
): Promise<void> {
  try {
    const ref = db.ref(`${entityType === 'patient' ? 'patients' : 'patient_follow_ups'}/${firebaseId}`);
    await ref.set(payload);
  } catch {
    await pool.execute(
      'INSERT INTO sync_queue (entityType, firebaseId, operation, payload) VALUES (?, ?, ?, ?)',
      [entityType, firebaseId, 'upsert', JSON.stringify(payload)]
    );
  }
}
```

### 14.2 Pull on Startup

When the Next.js API server starts (or when `/api/sync/pull` is called), it:

1. Reads all records from `/patients` and `/patient_follow_ups` in Firebase
2. For each record, checks if `firebaseId` exists in MySQL
3. If it exists: compares `updatedAt` — if Firebase has a newer timestamp, updates MySQL
4. If it doesn't exist: inserts as a new row (record was created on Android while web was offline)
5. If `deleted: true` and `deletedAt` is newer than MySQL `updatedAt`: soft-deletes in MySQL (or hard-deletes — TBD based on preference)

### 14.3 Periodic Background Pull

A lightweight polling mechanism runs every 5 minutes while the web app is running:

```typescript
// Called from a Next.js startup hook or a simple setInterval in a long-lived API route
setInterval(() => {
  fetch('/api/sync/pull', { method: 'POST' });
}, 5 * 60 * 1000);
```

For a local app on a single laptop, a simple interval is sufficient. No WebSocket or Firebase `onValue()` listener is needed because the web app is not expected to have multiple browser sessions open simultaneously.

### 14.4 Offline Queue Drain

`/api/sync/push` drains the `sync_queue` table:

1. Reads up to 50 pending rows from `sync_queue` ordered by `createdAt ASC`
2. For each row, attempts the Firebase write
3. On success: deletes the row from `sync_queue`
4. On failure: increments `attempts`; rows with `attempts >= 5` are flagged for manual review

The sync status badge in the navbar shows the pending count from `sync_queue`.

### 14.5 Full Re-Sync

`/api/sync/full` is a manual-trigger operation that:

1. Reads **all** patients and follow-ups from MySQL that have a `firebaseId`
2. Pushes each to Firebase unconditionally (overwriting Firebase state)
3. Use case: recovering from a state where Firebase data was accidentally deleted or corrupted

---

## 15. Conflict Resolution

**Strategy**: Last-write-wins by `updatedAt` (Unix milliseconds).

| Scenario | Resolution |
|---|---|
| Web app edits patient; Android edits same patient while web is offline | Whichever edit has the higher `updatedAt` overwrites the other on next sync |
| Android creates patient with localId=5; web creates different patient with localId=5 | No collision — each generates its own UUID `firebaseId`; Firebase stores them under different keys |
| Web deletes patient; Android opens the patient detail screen | On next Android sync: `deleted: true` with a `deletedAt` newer than Android's local `updatedAt` triggers deletion on Android |
| Web creates patient while offline; Android creates same-name patient while web is offline | Two separate records in Firebase after both come online — same person, two records. This is a **data quality issue**, not a sync bug. Resolution: manual merge (out of scope for V1) |

**`updatedAt` must be set on every mutation** — both on the web app (MySQL `ON UPDATE CURRENT_TIMESTAMP`) and in the Android app (explicit assignment in the DAO before insert/update).

---

## 16. Android App Changes Required

The Android app at `PatientRecords/` must be updated to participate in the new sync contract. These changes are required before the sync layer can work correctly end-to-end.

### 16.1 Room Database — version 3 → 4

Add two fields to both entities:

```kotlin
// In Patient.kt
@Entity(tableName = "patient_table")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firebaseId: String = "",      // NEW — UUID assigned on first sync
    val updatedAt: Long = 0L,         // NEW — Unix millis, set on every insert/update
    // ... all existing fields unchanged
)

// In PatientFollowUp.kt
@Entity(tableName = "patient_followup_table")
data class PatientFollowUp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firebaseId: String = "",
    val patientFirebaseId: String = "",  // NEW — link to parent patient UUID
    val updatedAt: Long = 0L,
    // ... all existing fields unchanged
)
```

**Room migration**:
```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE patient_table ADD COLUMN firebaseId TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE patient_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE patient_followup_table ADD COLUMN firebaseId TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE patient_followup_table ADD COLUMN patientFirebaseId TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE patient_followup_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
    }
}
```

### 16.2 FirebaseSyncManager.kt changes

- **Push**: before writing to Firebase, check if `firebaseId` is empty. If empty, generate a UUID (`UUID.randomUUID().toString()`), save it back to Room, then use it as the Firebase key.
- **Pull**: when reading from Firebase, match records by `firebaseId` (not by integer id). If no local record has that `firebaseId`, insert it as new. If one does, compare `updatedAt` and keep the newer version.
- **`updatedAt` on all writes**: every Room DAO `insert`/`update` call must set `updatedAt = System.currentTimeMillis()`.
- **Soft-delete support**: check `deleted == true` on pulled records and delete from Room accordingly.

### 16.3 Existing records migration (one-time)

On first launch after the update, existing Android records with `firebaseId == ""` must be assigned UUIDs:

```kotlin
// Run once in a coroutine on app startup (after Room migration)
val patientsWithoutId = patientDao.getPatientsWithoutFirebaseId()
patientsWithoutId.forEach { patient ->
    val uuid = UUID.randomUUID().toString()
    patientDao.update(patient.copy(firebaseId = uuid, updatedAt = System.currentTimeMillis()))
}
```

This is safe to run multiple times (it only touches rows where `firebaseId` is empty).

---

## 17. `lib/firebase.ts` — Firebase Admin Initialisation

```typescript
import * as admin from 'firebase-admin';

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
    }),
    databaseURL: process.env.FIREBASE_DATABASE_URL,
  });
}

export const firebaseDb = admin.database();
```

**Environment variables** in `.env.local` (never committed to git):
```
FIREBASE_PROJECT_ID=androidapp-70662
FIREBASE_CLIENT_EMAIL=<service-account-email>
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
FIREBASE_DATABASE_URL=https://androidapp-70662-default-rtdb.firebaseio.com
SESSION_SECRET=<32-char-random-string>
```

The service account JSON is downloaded from Firebase Console → Project Settings → Service Accounts → Generate new private key.

---

## 18. Implementation Phases

### Phase 0 — Project Bootstrap
- `npx create-next-app@latest WebPatientRecords --typescript --tailwind --app` (in `C:\xampp\htdocs\Hospital-Data-Web-App\`)
- Install: `shadcn/ui`, `next-themes`, `iron-session`, `mysql2`, `bcryptjs`, `react-hook-form`, `zod`, `lucide-react`, `sonner`, `uuid`, `firebase-admin`
- Configure Tailwind `darkMode: 'class'`, CSS variables in `globals.css`
- Set up `lib/db.ts` connection pool pointing to XAMPP MySQL
- Set up `lib/session.ts` with iron-session config
- Set up `lib/firebase.ts` with firebase-admin (credentials from `.env.local`)

### Phase 1 — Auth Layer
- `/api/auth/login` and `/api/auth/logout` routes
- iron-session session management
- `middleware.ts` protecting authenticated routes
- Login page with 21st.dev login component + theme toggle

### Phase 2 — Layout Shell
- App layout with 21st.dev sidebar + navbar
- Theme toggle wired to `next-themes`
- Route-based active nav highlighting
- Mobile hamburger sheet
- Logout button
- Sync status badge (static placeholder at this stage)

### Phase 3 — Dashboard
- `/api/dashboard` aggregating all 12 stats in one DB call
- Dashboard page with 21st.dev stat cards + Magic UI animations
- Recent patients + follow-ups tables (read-only at this stage)

### Phase 4 — Patient Records
- `/api/patients` GET (list, sort, paginate)
- Patients list page with 21st.dev table, sort pills, pagination
- Delete with AlertDialog → `/api/patients/[id]` DELETE (cascade)
- Search → `/api/search`

### Phase 5 — Patient CRUD
- `/api/patients` POST + `/api/patients/[id]` GET + PUT
- Add Patient form page (full 40-field form)
- Patient Details page (accordion follow-ups + initial details)
- Edit Patient form page
- Voice input re-integrated (Annyang loads as a client-side script)

### Phase 6 — Follow-ups
- `/api/patients/[id]/followups` GET + POST
- Add Follow-up page
- Follow-up display in patient details accordion

### Phase 7 — Polish
- Detailed Records page
- CSV export
- Magic UI animations tuned
- `prefers-reduced-motion` guard
- Loading skeletons on all data-fetching pages
- Error boundary pages (`error.tsx`, `not-found.tsx`)
- Final light/dark theme QA pass across all pages

### Phase 8 — MySQL Schema Migration (sync prep)
- Run `ALTER TABLE` migrations from §13 to add `firebaseId`, `updatedAt`, `sync_queue`
- Backfill `firebaseId` for all existing MySQL rows with `UUID()`
- Set `updatedAt` for all existing rows to `NOW()`
- Verify PHP web app still works (it ignores the new columns)

### Phase 9 — Firebase Sync Layer
- Implement `lib/firebase.ts` with firebase-admin initialisation
- Implement `lib/sync.ts` with `pushToFirebase()` and `pullFromFirebase()`
- Wire write-through into `/api/patients` and `/api/patients/[id]/followups` routes
- Implement `/api/sync/push`, `/api/sync/pull`, `/api/sync/status`, `/api/sync/full`
- Sync status badge wired to live `/api/sync/status`
- Manual "Sync Now" button on dashboard wired to `/api/sync/push` + `/api/sync/pull`
- Test with Android app: create patient on web → verify appears on Android; create on Android → verify appears on web after pull
- Update Android app (§16 changes) in `PatientRecords/`

---

## 19. Migration Strategy

1. Run both apps in parallel: old PHP app on `localhost:80` (XAMPP), new Next.js app on `localhost:3000`
2. Both apps share the same MySQL `hospital` database — no data migration needed for Phase 0–7
3. Migrate and test one page group at a time (auth → dashboard → records → CRUD → follow-ups → sync)
4. Once all pages are verified in Next.js, stop XAMPP Apache (MySQL stays running via XAMPP)
5. Point `localhost` at the Next.js app (or run `next start` on a fixed port)
6. PHP files remain in `Web-App/` as a reference backup; delete when fully verified

**Note on the PHP + MySQL parallel period**: the PHP app does not set `updatedAt` or `firebaseId`. Any records created via PHP during the parallel period will have `NULL` for both. Before going live with the sync layer (Phase 9), run the backfill SQL from §13 to assign UUIDs and timestamps to all existing rows.

---

## 20. Notes for 21st.dev Component Installation

Components are installed via the shadcn CLI:
```bash
npx shadcn@latest add "https://21st.dev/r/<component-slug>"
```

Before picking specific component slugs, browse the live 21st.dev gallery at:
- `21st.dev/s/navbar` — pick the navbar that best fits the clinic aesthetic
- `21st.dev/s/sidebar` — pick a collapsible sidebar with icon support  
- `21st.dev/s/dashboard` — pick stat cards with icon + number layout
- `21st.dev/s/table` — pick a sortable, paginated table
- `21st.dev/s/login` — pick a split-panel login layout
- `21st.dev/s/form` — pick a multi-section form layout

**Recommended aesthetic**: look for components tagged "minimal", "clean", or "enterprise" — avoid overly playful designs for a medical records app. The colour palette should use the clinic blue (`#2563EB`) as the primary accent.
