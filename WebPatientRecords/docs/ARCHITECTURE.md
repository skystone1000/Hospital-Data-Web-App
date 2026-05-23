# Architecture — WebPatientRecords

Mahajan Homeo Clinic — Next.js web app for patient record management.

---

## 1. Overview

A server-rendered Next.js 14 (App Router) application that shares the same `hospital` MySQL database as the legacy PHP Web-App. It provides a modern, typed, dark-mode-capable interface for patient intake, follow-up tracking, and clinic statistics.

| Concern | Choice | Version |
|---|---|---|
| Framework | Next.js App Router | 14.2.35 |
| Language | TypeScript (strict) | 5.x |
| Styling | Tailwind CSS | 3.4.x |
| Components | shadcn/ui (Radix primitives) | various |
| Theme | next-themes | 0.4.x |
| Session | iron-session | 8.x |
| Database | mysql2 (promise) | 3.x |
| Forms | react-hook-form + zod | 7.x / 4.x |
| Auth crypto | bcryptjs | 3.x |

---

## 2. Request / Response Flow

### General Pattern
All page routes are React Server Components that fetch data directly. Client components handle interactive features only.

```
Browser
  ↓ GET /dashboard
Next.js App Router
  ↓ middleware.ts  →  session check  →  redirect /login if unauthenticated
  ↓ (dashboard)/layout.tsx  →  reads session, renders sidebar + navbar shell
  ↓ (dashboard)/dashboard/page.tsx  →  SQL queries → JSX → HTML
  ↓ Response
```

### Auth Flow
```
/login  ──POST /api/auth/login──►  loginAdmin()
                                        │
                              SELECT admin_users
                              WHERE email OR uid
                                        │
                              bcrypt.compare()
                              (plaintext → rehash on match)
                                        │
                              iron-session.save()
                                        │
                              { ok: true }  →  client redirects to /dashboard
```

### Patient CRUD Flow
```
Client form submit
  ↓ fetch() POST /api/patients
  ↓ route.ts: zod.safeParse() → pool.execute(INSERT)
  ↓ { id: insertId }  →  router.push(/patients/[id])
```

---

## 3. Route Map

```
/                            →  redirect to /dashboard

/login                       →  (auth) group — no session guard
  POST /api/auth/login        →  loginAdmin(), sets iron-session cookie
  POST /api/auth/logout       →  destroys session, redirects /login

/dashboard                   →  stat cards (today/week/month/year) + recent tables
/patients                    →  sortable, searchable, paginated patient list
/patients/new                →  add patient form
/patients/[id]               →  patient detail view + follow-up accordion
/patients/[id]/edit          →  edit patient form
/patients/[id]/followup      →  add follow-up form
/records                     →  wide all-column table + client-side CSV export

GET  /api/dashboard          →  period stats (parallel Promise.all)
GET  /api/search?q=          →  LIKE search, returns top 20 for navbar
GET  /api/patients           →  paginated list with sort/search
POST /api/patients           →  create patient
GET  /api/patients/[id]      →  single patient
PUT  /api/patients/[id]      →  update patient
DEL  /api/patients/[id]      →  cascade delete (transaction)
GET  /api/patients/[id]/followups  →  follow-ups list
POST /api/patients/[id]/followups  →  create follow-up
```

---

## 4. Authentication & Session

`iron-session` stores an encrypted, signed cookie (`clinic_session`). The payload is defined in `lib/session.ts`:

```typescript
interface SessionData {
  adminId?: number;
  adminUid?: string;
  firstName?: string;
  lastName?: string;
}
```

**Two-layer auth guard:**

1. **`middleware.ts`** — runs on every request via Next.js Edge runtime. Reads the cookie and redirects to `/login` for unauthenticated page requests, returns 401 for unauthenticated API requests. Public paths: `/login`, `/api/auth/login`.
2. **`app/(dashboard)/layout.tsx`** — server component that re-checks the session; redirects if somehow bypassed.

**Password migration:** `lib/auth.ts` detects plaintext passwords (stored in the legacy PHP app) and re-hashes them with bcrypt on the first successful login.

---

## 5. Database Connection

`lib/db.ts` creates a single `mysql2` promise pool shared across all route handlers (module-level singleton, safe in Node.js). Key option:

```typescript
dateStrings: true   // Returns DATE/DATETIME as strings, not JS Date objects
```

Without `dateStrings: true`, mysql2 returns `Date` objects which React cannot render directly.

All queries use parameterised `pool.execute(sql, params)` — never string interpolation. Sort column injection is prevented by a `SORT_WHITELIST` array checked before interpolating the column name into `ORDER BY`.

**`ExecuteValues` constraint:** mysql2's `ExecuteValues` type excludes `undefined`. All optional zod fields (`string | undefined`) must be null-coerced before passing to `pool.execute()`:
```typescript
[d.firstName, d.middleName ?? null, ...]
```

---

## 6. Theme System

Dark mode uses Tailwind's `class` strategy + next-themes.

- `tailwind.config.ts` — `darkMode: "class"` + full HSL color token set (`background`, `foreground`, `primary`, `muted`, `card`, `popover`, `sidebar`, `accent`, `destructive`, `border`, `ring`)
- `app/globals.css` — defines `:root` (light) and `.dark` CSS variable blocks with HSL values
- `components/theme-provider.tsx` — wraps the app in `<NextThemesProvider attribute="class">`
- `components/layout/theme-toggle.tsx` — Sun/Moon button using `useTheme()`

Primary color: clinic blue `hsl(221 83% 53%)` in light, `hsl(217 91% 60%)` in dark.

---

## 7. Component Architecture

```
components/
├── theme-provider.tsx        Client — next-themes wrapper
├── layout/
│   ├── app-sidebar.tsx       Client — sticky left nav; active link via usePathname()
│   ├── app-navbar.tsx        Client — sticky top bar; mobile Sheet hamburger; quick search
│   └── theme-toggle.tsx      Client — dark/light toggle
├── patients/
│   ├── patient-form.tsx      Client — 37-field react-hook-form form (add + edit)
│   ├── delete-patient-button.tsx  Client — AlertDialog confirm + DELETE fetch
│   └── patients-search.tsx   Client — debounced (350ms) URL search param update
└── ui/                       shadcn/ui generated primitives (do not hand-edit)
    accordion, alert-dialog, badge, button, card,
    dropdown-menu, input, label, select, separator,
    sheet, skeleton, textarea
```

Server components (pages) handle all data fetching. Client components are used only where interactivity is required.

---

## 8. Data Types

All shared interfaces live in `types/index.ts`:

| Type | Purpose |
|---|---|
| `Patient` | Full patient record row |
| `FollowUp` | Follow-up data row |
| `DashboardStats` | Aggregated period stats |
| `PeriodStats` | count/earnings for one period |
| `AdminUser` | Admin row (no password) |
| `PaginatedPatients` | List API response envelope |

---

## 9. Known Constraints

- `z.coerce.number()` in the zod schema makes the resolver input type `unknown`, requiring an `as any` cast on `useForm<PatientInput>({ resolver: zodResolver(patientSchema) as any })`.
- `paid` and `balance` are stored as `VARCHAR` in MySQL (legacy schema). Earnings are computed with `SUM(CAST(paid AS DECIMAL(10,2)))`.
- `follow_up_num` is also `VARCHAR` — ordering requires `ORDER BY CAST(follow_up_num AS UNSIGNED)`.
- No transactions on reads. The follow-up insert uses `MAX(CAST(...)) + 1` in a single connection; concurrent inserts could produce duplicate numbers (low risk for single-clinic use).
- Firebase sync layer is planned but not yet implemented (`firebaseId`, `updatedAt` columns exist in `types/index.ts` but are not yet in the live schema).
