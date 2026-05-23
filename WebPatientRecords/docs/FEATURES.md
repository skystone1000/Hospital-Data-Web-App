# Features — WebPatientRecords

Status legend: `[x]` Implemented  `[~]` Partial  `[ ]` Planned

---

## Authentication & Session

| Feature | Status | Notes |
|---|---|---|
| Login with email or username | `[x]` | `lib/auth.ts` — queries by `email_admin OR uid_admin` |
| Bcrypt password verification | `[x]` | Transparent migration: plaintext → bcrypt on first login |
| Encrypted session cookie | `[x]` | iron-session v8; `httpOnly`, `sameSite: lax`; 24-hour TTL |
| Route guard (middleware) | `[x]` | `middleware.ts` — edge runtime; redirects pages, 401s APIs |
| Layout-level session guard | `[x]` | `(dashboard)/layout.tsx` secondary check |
| Logout | `[x]` | POST `/api/auth/logout` → `session.destroy()` → redirect `/login` |
| Admin self-registration | `[ ]` | Accounts created directly in DB |
| Password reset | `[ ]` | Not implemented |

---

## Patient Management

| Feature | Status | Notes |
|---|---|---|
| Add new patient | `[x]` | 37-field form in `PatientForm`; POST `/api/patients`; zod validation |
| View patient list | `[x]` | 20 per page; sortable; searchable |
| View patient details | `[x]` | Summary cards + follow-up accordion + collapsible full intake |
| Edit patient | `[x]` | Pre-populated `PatientForm`; PUT `/api/patients/[id]` |
| Delete patient | `[x]` | AlertDialog confirmation; cascade DELETE in DB transaction |
| Search patients | `[x]` | Debounced (350ms) URL param; LIKE on firstName, lastName, regno |
| Sort patients | `[x]` | Columns: firstName, lastName, dateJoined, regno, diagnosis; whitelist-guarded |
| Paginate patient list | `[x]` | 20/page; prev/next links; total count displayed |
| Patient registration number | `[x]` | Manual entry field (`regno`) |
| Detailed records view | `[x]` | `/records` — all columns in wide table |
| Export records to CSV | `[x]` | Client-side; inline `<script>` on `/records` page |
| Patient photo upload | `[ ]` | Not implemented |
| Bulk CSV import | `[ ]` | Not implemented |

---

## Follow-up Management

| Feature | Status | Notes |
|---|---|---|
| Add follow-up | `[x]` | POST `/api/patients/[id]/followups`; auto follow_up_num via `MAX(CAST(...) AS UNSIGNED) + 1` |
| View follow-ups per patient | `[x]` | Radix Accordion; ordered by `CAST(follow_up_num AS UNSIGNED) DESC` |
| Follow-up fields | `[x]` | weight, treatment_output, other_complains, treatment, medicine_duration, paid, balance |
| Edit existing follow-up | `[ ]` | Not implemented |
| Delete follow-up | `[ ]` | Follow-ups are cascade-deleted when patient is deleted |

---

## Dashboard & Statistics

| Feature | Status | Notes |
|---|---|---|
| 4-period stat cards | `[x]` | Today / This Week / This Month / This Year |
| New patients count per period | `[x]` | COUNT on patient_data |
| Follow-ups count per period | `[x]` | COUNT on follow_up_data |
| Earnings per period | `[x]` | SUM(CAST(paid AS DECIMAL)) from both tables combined |
| Recent new patients table | `[x]` | Last 7 days, limit 10, links to patient detail |
| Recent follow-ups table | `[x]` | Last 7 days, limit 10, JOIN with patient_data |
| Time-based greeting | `[x]` | Good morning / afternoon / evening based on server time |
| Charts / graphs | `[ ]` | Not implemented |
| Appointment calendar | `[ ]` | Not implemented |

---

## UI & Theming

| Feature | Status | Notes |
|---|---|---|
| Dark mode | `[x]` | next-themes; `attribute="class"`; persisted to localStorage |
| Light/dark theme toggle | `[x]` | Sun/Moon button in top navbar |
| Custom HSL color tokens | `[x]` | Full shadcn token set in `globals.css`; clinic-blue primary |
| Responsive layout | `[x]` | Tailwind breakpoints; columns hide/show at sm/md/lg |
| Mobile sidebar (Sheet) | `[x]` | Hamburger menu in navbar; Radix Sheet overlay |
| Sticky sidebar + navbar | `[x]` | Sidebar fixed on desktop; navbar sticky top |
| Fade-in page animation | `[x]` | `animate-fade-in` Tailwind keyframe on all page roots |
| Active nav link highlight | `[x]` | `usePathname()` in sidebar |
| Quick search (navbar) | `[x]` | GET `/api/search?q=`; links to patient pages |

---

## Security

| Feature | Status | Notes |
|---|---|---|
| SQL injection protection | `[x]` | All queries use parameterised `pool.execute()` |
| Sort column injection protection | `[x]` | `SORT_WHITELIST` array; only whitelisted values interpolated into ORDER BY |
| XSS protection | `[x]` | React escapes all rendered values by default |
| bcrypt password storage | `[x]` | `bcryptjs` cost factor 12; transparent migration |
| httpOnly session cookie | `[x]` | Not accessible to client-side JavaScript |
| Session auth on all API routes | `[x]` | middleware.ts returns 401 for unauthenticated API calls |
| CSRF protection | `[~]` | `sameSite: lax` cookie mitigates most CSRF; no explicit token |
| HTTPS enforcement | `[ ]` | `secure: true` cookie only in `NODE_ENV=production`; TLS handled by hosting |
| Rate limiting on login | `[ ]` | Not implemented |

---

## Planned Features (Firebase Sync)

| Feature | Status | Notes |
|---|---|---|
| `firebaseId` + `updatedAt` columns in MySQL | `[ ]` | Phase 8 migration |
| `sync_queue` table | `[ ]` | Phase 8 migration |
| `lib/firebase.ts` write layer | `[ ]` | Phase 9 |
| `/api/sync/push` — web → Firebase | `[ ]` | Phase 9 |
| `/api/sync/pull` — Firebase → MySQL | `[ ]` | Phase 9 |
| Last-write-wins conflict resolution | `[ ]` | Via `updatedAt` timestamp comparison |
