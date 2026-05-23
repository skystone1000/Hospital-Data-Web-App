# Claude Instructions — WebPatientRecords

## Documentation Policy

**Always read these docs first before touching any code:**

1. [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — system design, auth flow, DB conventions
2. [`docs/CODEBASE.md`](docs/CODEBASE.md) — file layout and module responsibilities
3. [`docs/FEATURES.md`](docs/FEATURES.md) — feature status table

**After every code change, update the relevant docs before closing the task:**

### When to update `docs/ARCHITECTURE.md`
- New library added or existing one replaced
- Auth or session mechanism changes
- Database connection options change (e.g. new pool config)
- New request/response flow (e.g. a new API pattern or middleware)
- Theme system changes
- New architectural constraint or decision documented

### When to update `docs/CODEBASE.md`
- New file or directory created
- File renamed or moved
- Existing file's purpose changes significantly
- New environment variable added
- New naming convention established
- New module/helper added to `lib/`

### When to update `docs/FEATURES.md`
- New feature implemented → change `[ ]` to `[x]` or add new row
- Partial feature completed → change `[ ]` to `[~]` or `[x]`
- Feature changed or removed → update the row
- New planned feature identified → add row with `[ ]`
- Bug fixed that was previously blocking a feature

### When to update `README.md`
- Setup steps change (new env variable, new prerequisite, new DB migration needed)
- New common task worth documenting
- New troubleshooting entry discovered
- Scripts change

---

## Coding Conventions

- All database queries use `pool.execute(sql, params)` with parameterised values — never string interpolation for user data.
- Sort columns interpolated into `ORDER BY` must be checked against a `SORT_WHITELIST` constant first.
- Optional zod fields (`string | undefined`) must be null-coerced with `?? null` before passing to `pool.execute()` — mysql2's `ExecuteValues` type excludes `undefined`.
- `dateStrings: true` in the pool config ensures DATE/DATETIME columns return as strings, not JS Date objects.
- Server components fetch data directly with `await pool.execute()`. Client components fetch via the API routes.
- New pages go under `app/(dashboard)/` to inherit the session guard. Public pages go under `app/(auth)/`.
- New shared UI primitives go in `components/ui/` as shadcn components. Feature-specific components go in `components/patients/` or a new `components/<feature>/` directory.
- New shared types go in `types/index.ts`.

---

## Adding a New Patient Form Field

1. Add the MySQL column to `patient_data`
2. Add the zod field to `patientSchema` in `lib/validations.ts`
3. Add the TypeScript field to `Patient` in `types/index.ts`
4. Add the form field to `PatientForm` in `components/patients/patient-form.tsx`
5. Add `fieldName ?? null` to the INSERT array in `app/api/patients/route.ts`
6. Add `fieldName ?? null` to the UPDATE array in `app/api/patients/[id]/route.ts`
7. Update `docs/FEATURES.md` (add to Patient Management table)
8. Update `docs/CODEBASE.md` if the field represents a new data category
