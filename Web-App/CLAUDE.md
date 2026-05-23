# Claude Instructions

## Documentation Policy

**Before reading any project file**, always read the docs first to understand the codebase:

1. Read [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — system structure and design decisions
2. Read [`docs/CODEBASE.md`](docs/CODEBASE.md) — file layout and module responsibilities
3. Read [`docs/FEATURES.md`](docs/FEATURES.md) — implemented features and their status
4. Read [`docs/audit.md`](docs/audit.md) — audit log and change history

**After any feature addition or major code change**, update the relevant doc files:

- New feature or changed behavior → update `docs/FEATURES.md`
- Structural/architectural changes → update `docs/ARCHITECTURE.md`
- New files, modules, or renamed paths → update `docs/CODEBASE.md`
- Any significant change → append an entry to `docs/audit.md`
