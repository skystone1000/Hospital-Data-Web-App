# UI Refactor Plan — Mahajan Homeo Clinic Web App

**Date**: 2026-05-23  
**Approach**: Full React (Next.js) frontend + Next.js API routes, retiring PHP. MySQL stays.  
**Component source**: [21st.dev](https://21st.dev) (React + Tailwind CSS, Next.js-ready)  
**Theming**: Light / Dark mode via `next-themes` + Tailwind CSS variables

---

## 1. Overview

The current app is a PHP-rendered Bootstrap site. This refactor replaces it entirely with a **Next.js 14 App Router** application that:

- Uses **21st.dev** components for every major UI surface
- Exposes all data operations as **Next.js API routes** (replacing PHP handlers)
- Connects directly to the existing **MySQL `hospital` database** via `mysql2`
- Supports **light and dark themes** with a toggle in the navbar, persisted to localStorage
- Is fully **responsive** (mobile → desktop) via Tailwind CSS

The XAMPP MySQL server continues running. XAMPP's PHP/Apache is no longer needed once the Next.js app is running.

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

---

## 3. Project Structure

```
clinic-app/                          ← new Next.js project (alongside or replacing Web-App/)
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
│   │   └── search/
│   │       └── route.ts             ← Search patients
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
│   └── validations.ts               ← zod schemas for all forms
└── types/
    └── index.ts                     ← Patient, FollowUp, Admin interfaces
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
```

### Validation
Every `POST` and `PUT` route validates the request body against a **zod schema** before touching the database. Invalid payloads return `{ error: "...", fields: {...} }` with HTTP 422.

### Error handling
All routes wrap DB calls in `try/catch`. Errors are logged server-side; clients receive a generic `{ error: "Internal server error" }` with HTTP 500. No SQL or stack traces are ever sent to the client.

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

All queries use **parameterised prepared statements** (same as the PHP fix already applied):
```typescript
const [rows] = await pool.execute(
  'SELECT * FROM patient_data WHERE id = ?',
  [id]
);
```

The existing `hospital` MySQL database schema is used as-is. No migrations needed for the initial refactor. Schema improvements (DECIMAL columns, INT follow_up_num) can be applied as a separate migration step.

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
  /* ... */
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

## 11. Implementation Phases

### Phase 0 — Project Bootstrap
- `npx create-next-app@latest clinic-app --typescript --tailwind --app`
- Install: `shadcn/ui`, `next-themes`, `iron-session`, `mysql2`, `bcryptjs`, `react-hook-form`, `zod`, `lucide-react`, `sonner`
- Configure Tailwind `darkMode: 'class'`, CSS variables in `globals.css`
- Set up `lib/db.ts` connection pool pointing to XAMPP MySQL
- Set up `lib/session.ts` with iron-session config

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

---

## 12. Migration Strategy

1. Run both apps in parallel: old PHP app on `localhost:80` (XAMPP), new Next.js app on `localhost:3000`
2. Both apps share the same MySQL `hospital` database — no data migration needed
3. Migrate and test one page group at a time (auth → dashboard → records → CRUD → follow-ups)
4. Once all pages are verified in Next.js, stop XAMPP Apache (MySQL stays running)
5. Point `localhost` at the Next.js app (or run `next start` on a fixed port)
6. PHP files remain in `Web-App/` as a reference backup; delete when fully verified

---

## 13. Firebase Integration Readiness

The Next.js architecture is designed to make the future Firebase migration straightforward:

- All DB calls are isolated in API routes — swap `mysql2` for `firebase-admin` Firestore calls one route at a time
- `lib/db.ts` is the only file that knows about MySQL — single place to change
- `iron-session` can be swapped for Firebase Auth (client SDK) without touching page components
- Next.js API routes can act as a sync bridge: write to MySQL AND Firebase during a transition period
- Tailwind/21st.dev UI has zero dependency on the data layer — frontend changes nothing when backend migrates

---

## 14. Notes for 21st.dev Component Installation

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
