# AnalyticalRx Data Platform Service

This repository contains the AnalyticalRx Data Platform Service design and
Spring Boot implementation.

Technology and scope:

- Java 21
- Spring Boot 3.5.16
- Private PostgreSQL through an SSM tunnel for local development
- JWT bearer authentication
- tenant/pharmacy role-based access control
- Phase 1 starter dashboard APIs

Start with:

- [Run locally](#local-setup) for environment setup and startup commands.
- [Implemented UI API contract](docs/ui-api-contract.md) for current integration behavior.
- [Backend documentation index](docs/README.md) for migrations and service references.
- [Central documentation map](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/documentation-map.md) for onboarding, shared AWS/database setup, and system designs.

The [service design](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/data-platform-service-design.md)
is maintained centrally. Repository run instructions, implemented contracts, and
service-specific migrations are maintained here alongside the code.

The progress tracker is a September 3, 2026 snapshot, not a live status board.
Use the current code, tests, and assigned GitHub issue to understand what is
implemented and what is active.

## UI API contracts

Give the UI engineer [Implemented UI API Contract](docs/ui-api-contract.md) as
the source of truth for routes, request/response shapes, and access rules they
can integrate today. The [broader UI API Contracts draft](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/ui-api-contracts.md)
covers planned Phase 1 dashboards, screen data, and sample responses. It is a
design draft, not a list of currently available APIs; some routes, ID types,
and error shapes there do not yet match this service.

Product requirements, database design, and RBAC standards remain in the private
[`analytical-rx-documentation`](https://github.com/analyticalrx/analytical-rx-documentation)
repository.

## Local setup

Prerequisites: Java 21, AWS CLI access to the AnalyticalRx development account,
and the AWS Session Manager Plugin. On Linux, install `jq` for the setup script.
The included Maven wrapper downloads and runs the required Maven version; a
separate Maven install is not needed.

Start the database tunnel in one terminal and leave it open. Follow the simple
Windows or macOS/Linux steps in the [pgAdmin and database tunnel guide](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/operations/teammate-rds-pgadmin-access-guide.md).
The backend uses the same local tunnel on port `5433`.

### Windows

1. Configure your own AWS CLI profile once. Use your personal AWS access keys:

   ```powershell
   aws configure --profile my-analyticalrx
   ```

   Enter `us-east-1` for the default region. Do not use a teammate's keys.
2. Open PowerShell in this repository and start the backend:

   ```powershell
   .\scripts\run-local.ps1 -Profile my-analyticalrx
   ```

   If your AWS credentials are already the default profile, run
   `.\scripts\run-local.ps1` instead.

### Linux

1. In this repository, create the local environment file from AWS secrets. Add
   your profile name in place of `my-analyticalrx`:

   ```bash
   AWS_PROFILE=my-analyticalrx bash scripts/setup-vscode-env.sh
   ```

   The script checks the account, reads the database and JWT secrets, and writes
   them to a private, Git-ignored `.env`. It will not overwrite an existing
   `.env`. If you already have `.env`, skip this command. If you use default
   AWS credentials, run `bash scripts/setup-vscode-env.sh` instead.
2. In the same terminal, start the backend:

   ```bash
   set -a
   source .env
   set +a
   ./mvnw spring-boot:run
   ```

   Keep the database tunnel running in its other terminal. The `.env` file is
   also used by the Java launch configuration if you add one in VS Code.

The backend is available at `http://127.0.0.1:8080`. Never commit `.env` or
paste its values into source code, `launch.json`, or chat. The Windows script
reads secrets into the running process without printing them.

### Test login

```bash
curl -i -X POST http://127.0.0.1:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"tenantKey":"<tenant-key>","email":"<user-email>","password":"<password>"}'
```

### Test pharmacy access

Use the bearer token returned by login to list active pharmacies within the
user's scope, then fetch one by its `pharmacy_key`:

```bash
curl -i http://127.0.0.1:8080/api/v1/pharmacies \
  -H 'Authorization: Bearer <access-token>'

curl -i http://127.0.0.1:8080/api/v1/pharmacies/<pharmacy-key> \
  -H 'Authorization: Bearer <access-token>'
```

`OWNER` and `TENANT_ADMIN` see all active pharmacies in their tenant. Other
roles, including `STORE_MANAGER`, `BOOKKEEPER`, `PHARMACIST`, and `TECHNICIAN`,
see only active pharmacies assigned through `control_plane.user_pharmacy_access`.
`GET /api/v1/me` returns the same effective pharmacy keys. An unassigned
pharmacy returns 403 on the single-pharmacy endpoint; a missing or inactive
pharmacy returns 404 after the scope check. The list endpoint returns an empty
array when no active pharmacies are in scope.

These endpoints return pharmacy metadata only. Pharmacy scope does not grant
access to operational, financial, or patient data: each future dashboard or
workflow API must also check the caller's permission for that content.

### Task management

Apply `database/manual-migrations/2026-10-03-task-management.sql` as a database
admin before using task APIs. This migration targets the dev tenant schema and
grants the data-platform database role access to the new tables. Apply the same
table design and role grants to each new tenant schema during onboarding.

Tasks belong to one tenant and pharmacy and require an assignee who can access
that pharmacy. `OWNER`, `STORE_MANAGER`, `PHARMACIST`, `BOOKKEEPER`, and
`BUSINESS_DEVELOPMENT` can create tasks. Assignees and managers can change task
status; `TECHNICIAN` can update assigned tasks but cannot create them.
`TENANT_ADMIN` has no task-content permission. Owners see all tasks in their
tenant; store managers see tasks in their assigned pharmacies; other roles see
tasks they created or that are assigned to them in an assigned pharmacy.
Closed tasks remain available for audit and cannot be reopened in this release.

The first release exposes:

```text
POST  /api/v1/tasks
GET   /api/v1/tasks?pharmacyKey=PHARM-001&status=TODO
GET   /api/v1/tasks/{id}
PATCH /api/v1/tasks/{id}/status
POST  /api/v1/tasks/{id}/comments
GET   /api/v1/tasks/{id}/comments
```

Example create body: `{"pharmacyKey":"PHARM-001","title":"Prepare refill queue","assigneeUserId":5,"priority":"HIGH"}`.
Example status body: `{"status":"IN_PROGRESS"}`. Hard deletion is intentionally
not exposed; creation, status changes, and comments are recorded in
`task_activity`.
