# Mahajan Homeo Clinic — Web Patient Records

A modern Next.js web app for managing homeopathic patient records. Built with TypeScript, shadcn/ui, Tailwind CSS, and MySQL.

---

## Prerequisites

| Tool | Version | Check |
|---|---|---|
| Node.js | 18.17+ | `node -v` |
| npm | 9+ | `npm -v` |
| MySQL / XAMPP | 5.7+ | Running on port 3306 |

The app connects to the same `hospital` database used by the legacy PHP Web-App. XAMPP must be running with MySQL active before you start the dev server.

---

## Quick Start

### 1. Install dependencies

```bash
cd WebPatientRecords
npm install
```

### 2. Configure environment variables

Create a `.env.local` file in the `WebPatientRecords/` root (one already exists if you cloned this repo):

```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=
DB_NAME=hospital
SESSION_SECRET=clinic-app-session-secret-key-min32!!
```

Change `SESSION_SECRET` to any random string of 32+ characters for security. For local development the defaults work out of the box with a standard XAMPP setup.

### 3. Verify the database

Make sure the `hospital` database exists and has the required tables. You can check in phpMyAdmin (`http://localhost/phpmyadmin`) or run:

```sql
SHOW TABLES IN hospital;
-- Expected: admin_users, patient_data, follow_up_data
```

If the tables are missing, import the setup SQL from `../Web-App/setup/` in the parent directory.

### 4. Run the development server

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000). You will be redirected to the login page.

### 5. Log in

Use the same admin credentials you use for the PHP Web-App. The username field accepts either the email address or the uid (username). On your first login the password is automatically migrated from plaintext to bcrypt — subsequent logins use bcrypt.

---

## Available Scripts

| Script | What it does |
|---|---|
| `npm run dev` | Start dev server at http://localhost:3000 with hot reload |
| `npm run build` | Production build (outputs to `.next/`) |
| `npm run start` | Start production server (requires `build` first) |
| `npm run lint` | Run ESLint (linting is skipped during `build`) |

---

## Project Structure at a Glance

```
WebPatientRecords/
├── app/
│   ├── (auth)/login/       Login page
│   ├── (dashboard)/        All authenticated pages
│   │   ├── dashboard/      Stats overview
│   │   ├── patients/       Patient list, add, view, edit
│   │   └── records/        Wide records table + CSV export
│   └── api/                REST API route handlers
├── components/
│   ├── layout/             Sidebar, Navbar, ThemeToggle
│   ├── patients/           PatientForm, DeleteButton, Search
│   └── ui/                 shadcn/ui primitives (don't edit)
├── lib/
│   ├── auth.ts             Login / logout logic
│   ├── db.ts               MySQL connection pool
│   ├── session.ts          iron-session helpers
│   └── validations.ts      Zod schemas
├── types/index.ts          Shared TypeScript interfaces
├── middleware.ts           Auth guard (runs on every request)
└── docs/                   Documentation
    ├── ARCHITECTURE.md
    ├── CODEBASE.md
    └── FEATURES.md
```

Full file-by-file documentation: [`docs/CODEBASE.md`](docs/CODEBASE.md)

---

## Key Concepts for New Developers

### Server vs Client Components

Next.js App Router defaults to **server components** — they run on the server, can `await` database calls directly, and produce HTML. You'll see them as plain `async function Page()` exports.

**Client components** are opted in with `"use client"` at the top of the file. Use them only when you need browser APIs, event handlers, or React hooks (`useState`, `useEffect`, etc.). Most pages in this app are server components; the forms and search are client components.

### How data flows

Page (server) → SQL query → renders JSX → sent as HTML to browser  
User action → client component → `fetch("/api/...")` → route handler → SQL → JSON response

### How auth works

Every request passes through `middleware.ts` before hitting any page or API. The middleware reads an encrypted cookie (`clinic_session`) using iron-session. If the cookie is missing or invalid, it redirects to `/login` (pages) or returns 401 (API routes).

### How forms work

Forms use [react-hook-form](https://react-hook-form.com/) with [zod](https://zod.dev/) for validation. The schemas live in `lib/validations.ts`. When a form is submitted, it calls a `fetch()` to the relevant API route; on success it navigates to the detail page.

### Dark mode

Click the Sun/Moon icon in the top-right of the navbar. The preference is saved in `localStorage` and applied via a CSS class on `<html>`. All colors are HSL CSS variables defined in `app/globals.css`.

---

## Common Tasks

### Add a new field to the patient form

1. Add the column to the MySQL `patient_data` table.
2. Add the field to `patientSchema` in `lib/validations.ts`.
3. Add the field to the `Patient` interface in `types/index.ts`.
4. Add the field to `PatientForm` in `components/patients/patient-form.tsx`.
5. Add `?? null` coercion for the field in the INSERT and UPDATE arrays in `app/api/patients/route.ts` and `app/api/patients/[id]/route.ts`.
6. Update `docs/FEATURES.md` and `docs/CODEBASE.md`.

### Add a new page

1. Create `app/(dashboard)/your-page/page.tsx` — it will automatically be session-guarded by the dashboard layout.
2. Add a nav link in `components/layout/app-sidebar.tsx`.
3. Update `docs/FEATURES.md`.

### Change the primary color

Edit the `--primary` HSL values in `app/globals.css` — one value in `:root` (light mode) and one in `.dark`.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `Error: connect ECONNREFUSED 127.0.0.1:3306` | Start MySQL in XAMPP Control Panel |
| `Objects are not valid as a React child (Date)` | Ensure `dateStrings: true` is set in `lib/db.ts` |
| Login says "Invalid credentials" | Check that `admin_users` table exists and has a row |
| Blank page after login | Check browser console; usually a missing env variable |
| `SESSION_SECRET` warning | Set a 32+ character random string in `.env.local` |
| Port 3000 already in use | Run `npm run dev -- -p 3001` to use a different port |

---

## Documentation

| File | Purpose |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | System design, request flows, DB connection, auth, theme |
| [`docs/CODEBASE.md`](docs/CODEBASE.md) | Every file explained, conventions, env vars |
| [`docs/FEATURES.md`](docs/FEATURES.md) | Feature status table (implemented / planned) |

For the legacy PHP Web-App documentation, see `../Web-App/docs/`.
