# Codebase — WebPatientRecords

File layout, module responsibilities, and conventions.

---

## Directory Structure

```
WebPatientRecords/
├── app/                          Next.js App Router root
│   ├── (auth)/                   Route group — no session guard
│   │   └── login/page.tsx        Login page (client component)
│   ├── (dashboard)/              Route group — session-guarded layout
│   │   ├── layout.tsx            Shell: reads session, renders sidebar + navbar
│   │   ├── dashboard/page.tsx    Stats dashboard (server component)
│   │   ├── patients/
│   │   │   ├── page.tsx          Patient list (sortable, paginated, searchable)
│   │   │   ├── new/page.tsx      Add patient — renders <PatientForm />
│   │   │   └── [id]/
│   │   │       ├── page.tsx      Patient detail + follow-up accordion
│   │   │       ├── edit/page.tsx Edit patient — renders <PatientForm defaultValues>
│   │   │       └── followup/page.tsx  Add follow-up (client component)
│   │   └── records/page.tsx      Wide all-column table + CSV export
│   ├── api/
│   │   ├── auth/
│   │   │   ├── login/route.ts    POST — calls loginAdmin(), sets session cookie
│   │   │   └── logout/route.ts   POST — destroys session
│   │   ├── dashboard/route.ts    GET — 4-period stats via Promise.all
│   │   ├── search/route.ts       GET ?q= — LIKE search, limit 20
│   │   └── patients/
│   │       ├── route.ts          GET (list + pagination) / POST (create)
│   │       └── [id]/
│   │           ├── route.ts      GET / PUT / DELETE (cascade transaction)
│   │           └── followups/route.ts  GET / POST
│   ├── globals.css               HSL CSS variables (light + dark), base styles
│   ├── layout.tsx                Root layout — Inter font, ThemeProvider
│   └── page.tsx                  Root redirect to /dashboard
│
├── components/
│   ├── theme-provider.tsx        next-themes wrapper
│   ├── layout/
│   │   ├── app-sidebar.tsx       Sticky left sidebar, nav links, doctor name
│   │   ├── app-navbar.tsx        Top bar, mobile hamburger, quick search
│   │   └── theme-toggle.tsx      Sun/Moon theme switch button
│   ├── patients/
│   │   ├── patient-form.tsx      37-field intake + edit form
│   │   ├── delete-patient-button.tsx  Confirm dialog + DELETE request
│   │   └── patients-search.tsx   Debounced search input → URL param
│   └── ui/                       shadcn/ui generated primitives (Radix-based)
│
├── lib/
│   ├── auth.ts                   loginAdmin() / logoutAdmin() — bcrypt + session
│   ├── db.ts                     mysql2 pool (singleton, dateStrings: true)
│   ├── session.ts                iron-session helpers + SessionData type
│   ├── utils.ts                  cn() — clsx + tailwind-merge helper
│   └── validations.ts            zod schemas: loginSchema, patientSchema, followUpSchema
│
├── types/
│   └── index.ts                  Shared TS interfaces: Patient, FollowUp, AdminUser, etc.
│
├── middleware.ts                  Route guard — redirects unauthenticated requests
├── tailwind.config.ts             HSL token color system + custom animations
├── next.config.mjs                eslint.ignoreDuringBuilds: true
├── tsconfig.json                  strict: true, @/* path alias
├── .env.local                     Environment variables (not committed)
└── docs/                          This documentation directory
    ├── ARCHITECTURE.md
    ├── CODEBASE.md
    └── FEATURES.md
```

---

## Module Responsibilities

### `lib/db.ts`
Creates and exports a single `mysql2` promise pool. All route handlers import this pool directly. `dateStrings: true` ensures all `DATE`/`DATETIME` columns come back as strings.

### `lib/session.ts`
Exports `SessionData` interface, `sessionOptions` (cookie name + encryption password + TTL), and `getSession()` — a thin async wrapper around `getIronSession(await cookies(), sessionOptions)`. Use `getSession()` everywhere in server components and route handlers.

### `lib/auth.ts`
`loginAdmin(identifier, password)` — queries `admin_users` by email or uid, checks bcrypt (or migrates plaintext to bcrypt on first match), sets and saves the session. Returns `{ ok: true }` or `{ ok: false, error }`.

`logoutAdmin()` — destroys the session cookie.

### `lib/validations.ts`
Zod schemas. `optStr = z.string().optional()` is used for every nullable field. All optional fields infer as `string | undefined`, which is then null-coerced to `string | null` before being passed to `pool.execute()`.

```
loginSchema    →  LoginInput   (identifier, password)
patientSchema  →  PatientInput (37 fields)
followUpSchema →  FollowUpInput (7 fields)
```

### `types/index.ts`
Pure TypeScript interfaces for DB row shapes. No runtime code. Import from `@/types`.

### `middleware.ts`
Runs on every request (Edge runtime). Reads the iron-session cookie via `getIronSession(req, res, sessionOptions)`. Passes through `/login` and `/api/auth/login` without checking. Redirects unauthenticated page requests to `/login`, returns 401 JSON for unauthenticated API requests.

### `components/patients/patient-form.tsx`
Dual-mode form (create when `patientId` is undefined, update otherwise). Uses `useForm<PatientInput>` with `zodResolver(patientSchema) as any` (cast required due to `z.coerce.number()` input type being `unknown`). All 37 fields rendered in grouped `<Section>` cards.

### `components/patients/patients-search.tsx`
Controlled input. On change, sets a 350ms debounce timeout that builds a new `URLSearchParams` and calls `router.push()`. Resets `page` to `1` on every new search.

---

## Naming Conventions

| Pattern | Example |
|---|---|
| Page files | `app/(dashboard)/patients/page.tsx` |
| Route handlers | `app/api/patients/route.ts` |
| Client components | `"use client"` at top of file |
| Server components | No directive (default) |
| Shared UI primitives | `components/ui/*.tsx` (shadcn generated) |
| Feature components | `components/patients/*.tsx` |
| Layout components | `components/layout/*.tsx` |
| Lib helpers | `lib/*.ts` (no JSX) |
| Type file | `types/index.ts` |

## Path Alias

`@/` maps to the project root (`tsconfig.json` `paths`). Always use `@/` imports rather than relative paths.

## Environment Variables

Defined in `.env.local` (not committed). All have safe defaults for local development.

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_USER` | `root` | MySQL user |
| `DB_PASSWORD` | `` (empty) | MySQL password |
| `DB_NAME` | `hospital` | Database name |
| `SESSION_SECRET` | built-in fallback | iron-session encryption key (min 32 chars) |

**For production:** always override `SESSION_SECRET` with a random 32+ character string.
