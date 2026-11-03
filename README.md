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





## Doctor CRM

### Module 1: Doctor Office Management (Test Cases)

* **TC-OFF-01: Create Office (Valid)**
  * **Endpoint:** `POST /api/v1/doctor-offices`[cite: 1]
  * **Payload:**
    ```json
    {
      "officeName": "Sunrise Health Clinic",
      "doctorName": "Dr. Ravi Kumar",
      "specialty": "General Practice",
      "address": "123 Health Ave, Suite 400",
      "phone": "+1-555-0192",
      "relationshipStage": "NEW",
      "priority": "HIGH",
      "ownerId": "7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1"
    }
    ```
  * **Expected Status:** `201 Created`[cite: 1]
  * **Expected Result:** Office created with generated UUID and `version = 0`[cite: 1].

* **TC-OFF-02: Create Office Without Name (Validation Failure)**
  * **Endpoint:** `POST /api/v1/doctor-offices`[cite: 1]
  * **Payload:** Omit `officeName`[cite: 1].
  * **Expected Status:** `400 Bad Request`[cite: 1]
  * **Expected Result:** Validation failure indicating `officeName` is required.

* **TC-OFF-03: Create Office With Invalid Enum (Validation Failure)**
  * **Endpoint:** `POST /api/v1/doctor-offices`[cite: 1]
  * **Payload:** `"relationshipStage": "UNKNOWN_STAGE"`[cite: 1]
  * **Expected Status:** `400 Bad Request`[cite: 1]
  * **Expected Result:** Enum deserialization or validation error[cite: 1].

* **TC-OFF-04: Get Office by ID**
  * **Endpoint:** `GET /api/v1/doctor-offices/{officeId}`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns full doctor office profile details[cite: 1].

* **TC-OFF-05: Get Office by Non-Existent ID**
  * **Endpoint:** `GET /api/v1/doctor-offices/00000000-0000-0000-0000-000000000000`
  * **Expected Status:** `404 Not Found`
  * **Expected Result:** Entity not found error response.

* **TC-OFF-06: List All Offices**
  * **Endpoint:** `GET /api/v1/doctor-offices`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns JSON array containing registered clinic offices[cite: 1].

* **TC-OFF-07: Filter Offices by Relationship Stage**
  * **Endpoint:** `GET /api/v1/doctor-offices?stage=NEW`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns only offices where `relationshipStage` is `NEW`[cite: 1].

* **TC-OFF-08: Filter Offices by Owner ID**
  * **Endpoint:** `GET /api/v1/doctor-offices?ownerId=7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns only offices assigned to specified owner UUID[cite: 1].

* **TC-OFF-09: Update Office Profile**
  * **Endpoint:** `PUT /api/v1/doctor-offices/{officeId}`[cite: 1]
  * **Payload:**
    ```json
    {
      "officeName": "Sunrise Health Clinic - Main Branch",
      "doctorName": "Dr. Ravi Kumar",
      "specialty": "Internal Medicine",
      "address": "123 Health Ave, Suite 500",
      "phone": "+1-555-0192",
      "relationshipStage": "CONTACTED",
      "priority": "HIGH",
      "ownerId": "7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1"
    }
    ```
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns updated clinic details with refreshed `updatedAt`[cite: 1].

* **TC-OFF-10: Delete Office Profile**
  * **Endpoint:** `DELETE /api/v1/doctor-offices/{officeId}`
  * **Expected Status:** `204 No Content`
  * **Expected Result:** Office record is deleted from the database.

---

### Module 2: Visit & Interaction Management (Test Cases)

* **TC-INT-01: Record Visit Interaction**
  * **Endpoint:** `POST /api/v1/doctor-offices/{officeId}/interactions`[cite: 1]
  * **Payload:**
    ```json
    {
      "occurredAt": "2026-11-04T10:30:00Z",
      "visitorId": "7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1",
      "interactionType": "VISIT",
      "purpose": "Introduce platform pharmacy services",
      "pitchId": "cbe78bd8-9b2c-4e10-96ef-55d3fbd20d8c",
      "pitchSnapshot": "Standard clinic partnership pitch",
      "objections": "Doctor requested pricing model breakdown",
      "outcome": "FOLLOW_UP_REQUIRED",
      "notes": "Clinic coordinator interested in delivery schedules"
    }
    ```
  * **Expected Status:** `201 Created`[cite: 1]
  * **Expected Result:** Interaction saved with generated UUID, pitch snapshot, and objections[cite: 1].

* **TC-INT-02: Record Call Interaction**
  * **Endpoint:** `POST /api/v1/doctor-offices/{officeId}/interactions`[cite: 1]
  * **Payload:**
    ```json
    {
      "occurredAt": "2026-11-04T14:15:00Z",
      "visitorId": "7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1",
      "interactionType": "CALL",
      "purpose": "Verify receiving of documentation",
      "outcome": "POSITIVE",
      "notes": "Doctor confirmed receiving documents"
    }
    ```
  * **Expected Status:** `201 Created`[cite: 1]
  * **Expected Result:** Interaction persisted with outcome `POSITIVE`[cite: 1].

* **TC-INT-03: Record Interaction Against Non-Existent Office**
  * **Endpoint:** `POST /api/v1/doctor-offices/00000000-0000-0000-0000-000000000000/interactions`
  * **Expected Status:** `404 Not Found`
  * **Expected Result:** Returns error indicating doctor office does not exist.

* **TC-INT-04: Missing Purpose or Outcome (Validation Failure)**
  * **Endpoint:** `POST /api/v1/doctor-offices/{officeId}/interactions`[cite: 1]
  * **Payload:** Payload omitting `purpose` or `outcome`[cite: 1].
  * **Expected Status:** `400 Bad Request`[cite: 1]
  * **Expected Result:** Field validation failure on required properties.

* **TC-INT-05: Fetch Office Interaction Timeline**
  * **Endpoint:** `GET /api/v1/doctor-offices/{officeId}/interactions`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns interactions ordered chronologically (`occurredAt DESC`), latest first[cite: 1].

* **TC-INT-06: Fetch Timeline for Office with Zero Visits**
  * **Endpoint:** `GET /api/v1/doctor-offices/{newOfficeId}/interactions`
  * **Expected Status:** `200 OK`
  * **Expected Result:** Returns an empty JSON list `[]`.

---

### Module 3: Follow-Up Tasks & Calendar Activities (Test Cases)

* **TC-TSK-01: Schedule Follow-Up Task**
  * **Endpoint:** `POST /api/v1/follow-ups`[cite: 1]
  * **Payload:**
    ```json
    {
      "officeId": "{officeId}",
      "interactionId": "{interactionId}",
      "title": "Send workflow documentation pack",
      "activityType": "CALL",
      "nextAction": "Call clinic coordinator to clarify delivery window",
      "assignedTo": "7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1",
      "dueAt": "2026-11-10T14:00:00Z",
      "priority": "HIGH",
      "createdBy": "7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1"
    }
    ```
  * **Expected Status:** `201 Created`[cite: 1]
  * **Expected Result:** Task scheduled with default status `OPEN` and priority `HIGH`[cite: 1].

* **TC-TSK-02: Missing Required Task Fields (Validation Failure)**
  * **Endpoint:** `POST /api/v1/follow-ups`[cite: 1]
  * **Payload:** Payload missing `title`, `dueAt`, or `assignedTo`[cite: 1].
  * **Expected Status:** `400 Bad Request`
  * **Expected Result:** Validation failure indicating mandatory fields are missing.

* **TC-TSK-03: Fetch Open Tasks by Assigned User**
  * **Endpoint:** `GET /api/v1/follow-ups?assignedTo=7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns only tasks with status `OPEN` or `IN_PROGRESS` for the user[cite: 1].

* **TC-TSK-04: Query Overdue Follow-Ups**
  * **Endpoint:** `GET /api/v1/follow-ups?assignedTo=7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1&overdue=true`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns only open/in-progress tasks where `due_at < now()`[cite: 1].

* **TC-TSK-05: Complete Follow-Up Task**
  * **Endpoint:** `PATCH /api/v1/follow-ups/{taskId}`[cite: 1]
  * **Payload:**
    ```json
    {
      "status": "COMPLETED"
    }
    ```
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Status updated to `COMPLETED` and `completedAt` timestamp is populated[cite: 1].

* **TC-TSK-06: Verify Completed Task Excluded from Overdue**
  * **Endpoint:** `GET /api/v1/follow-ups?assignedTo=7e2f7a3a-6fb8-4e5d-a3c7-7b53c4a9b8a1&overdue=true`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Tasks with `status: "COMPLETED"` are omitted even if `dueAt` is in the past[cite: 1].

* **TC-TSK-07: Reschedule Task Due Date**
  * **Endpoint:** `PATCH /api/v1/follow-ups/{taskId}`[cite: 1]
  * **Payload:**
    ```json
    {
      "dueAt": "2026-11-20T10:00:00Z"
    }
    ```
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Task due date updated successfully[cite: 1].

* **TC-TSK-08: Fetch Calendar Activities in Date Range**
  * **Endpoint:** `GET /api/v1/calendar/activities?start=2026-11-01T00:00:00Z&end=2026-11-30T23:59:59Z`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns scheduled activities falling within the date window[cite: 1].

---

### Module 4: Pitch Reporting & Dashboard Analytics (Test Cases)

* **TC-REP-01: Pitch Outcomes Comparison Report**
  * **Endpoint:** `GET /api/v1/reports/pitch-outcomes`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns list of active pitches with total usage counts and breakdown map per outcome (`POSITIVE`, `DECLINED`, `FOLLOW_UP_REQUIRED`)[cite: 1].

* **TC-REP-02: Dashboard Summary (Default 30-Day Range)**
  * **Endpoint:** `GET /api/v1/dashboard/summary`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Returns metrics payload containing `totalInteractionsPeriod`, `followUpsDueToday`, `overdueFollowUps`, `completedFollowUpsPeriod`, and `relationshipStagesDistribution` map[cite: 1].

* **TC-REP-03: Dashboard Summary (Custom Date Window)**
  * **Endpoint:** `GET /api/v1/dashboard/summary?startDate=2026-11-01T00:00:00Z&endDate=2026-11-30T23:59:59Z`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** Aggregate counts scoped strictly within the provided date range[cite: 1].

* **TC-REP-04: Dashboard Overdue Task Metric Accuracy**
  * **Endpoint:** `GET /api/v1/dashboard/summary`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** `overdueFollowUps` matches count of all tasks where `status IN ('OPEN', 'IN_PROGRESS')` and `due_at < now()`[cite: 1].

* **TC-REP-05: Dashboard Relationship Stage Distribution Accuracy**
  * **Endpoint:** `GET /api/v1/dashboard/summary`[cite: 1]
  * **Expected Status:** `200 OK`[cite: 1]
  * **Expected Result:** `relationshipStagesDistribution` map accurately tallies offices grouped by `NEW`, `CONTACTED`, `ENGAGED`, `ACTIVE`, and `INACTIVE`[cite: 1].
