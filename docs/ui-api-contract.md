# UI API Contract: Implemented Endpoints

Status: implemented in this repository as of 2026-10-03. This is the contract
for UI integration **now**. The broader [Phase 1 UI contract draft](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/ui-api-contracts.md)
describes planned dashboards and admin screens; those routes are not live yet.

## Common rules

- Base URL locally: `http://127.0.0.1:8080/api/v1`.
- Send and receive JSON with `camelCase` field names.
- After login, send `Authorization: Bearer <accessToken>` on every other route.
- IDs are JSON numbers backed by PostgreSQL `bigint`; `pharmacyKey` and
  `tenantKey` are strings. The UI never sends database schema names.
- Date-times are ISO-8601 strings with offsets (usually UTC `Z`). Nullable
  fields are serialized as `null`.
- There is no stable common error response yet. Login failures return
  `{"error":"invalid_credentials","message":"Invalid email or password"}`;
  other failures currently use Spring's default error handling. Use HTTP status
  codes for now, not a parsed error code. Login failures return 401; other
  requests may return 400 for invalid input, 403 for insufficient access, 404
  for unavailable records, and 409 for a closed task status update. Authentication
  errors outside login still need a standardized response contract.

## Authentication and context

### `POST /auth/login`

No bearer token required. Request:

```json
{"tenantKey":"analyticalrx-dev","email":"owner@analyticalrx.dev","password":"<password>"}
```

Success: `200`.

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 1800,
  "issuedAt": "2026-10-03T09:00:00Z",
  "user": {"id": 1, "email": "owner@analyticalrx.dev", "displayName": "Owner", "status": "active"}
}
```

Invalid tenant, email, or password: `401`. Login is tenant-specific; submit
the selected `tenantKey` each time.

### `GET /me`

Returns the authenticated user, tenant ID, effective roles and permissions,
and accessible pharmacy keys. Arrays may be empty; their order is not a UI
contract. Example:

```json
{
  "user": {"id": 1, "email": "owner@analyticalrx.dev", "displayName": "Owner", "status": "active"},
  "tenantId": 1,
  "roles": ["OWNER"],
  "permissions": ["task.read", "task.create", "task.update", "task.comment"],
  "pharmacyKeys": ["PHARM-001", "PHARM-002"],
  "permissionsVersion": 1
}
```

### `POST /auth/logout`

Returns `{"status":"logged_out"}`. Currently this does **not** revoke an
issued JWT server-side; the UI must discard the token on logout.

## Pharmacies

### `GET /pharmacies`

Returns a JSON array of active pharmacies visible to the caller. Owners and
tenant admins see all active pharmacies in their tenant; other users see their
explicit active pharmacy assignments. Empty access returns `[]`.

```json
[
  {"pharmacyKey":"PHARM-001","displayName":"Example Pharmacy","stateCode":"NY","npi":"1900000001","ncpdp":"9000001"}
]
```

### `GET /pharmacies/{pharmacyKey}`

Returns one pharmacy object in the same shape. An unassigned pharmacy returns
`403`; a missing or inactive pharmacy returns `404` after scope validation.

## Tasks

Task APIs enforce both task permissions and pharmacy scope. Owners see tenant
tasks; store managers see tasks in assigned pharmacies; other permitted roles
see tasks they created or are assigned to in an assigned pharmacy. Tenant
admins do not have task-content access. Task IDs are numeric.

`status`: `TODO`, `IN_PROGRESS`, `BLOCKED`, `COMPLETED`, `SKIPPED`, `CANCELED`.
`priority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT` (defaults to `MEDIUM`).

### `POST /tasks`

Creates a task and returns a task object with status `TODO` (`201`). Allowed
creators: owner, store manager, pharmacist, bookkeeper, business development.
The assignee must be an active user with task-read permission and access to
the specified pharmacy.

```json
{
  "pharmacyKey": "PHARM-001",
  "title": "Prepare refill queue",
  "description": "Review today's scheduled refills",
  "assigneeUserId": 5,
  "priority": "HIGH",
  "dueAt": "2026-10-04T09:00:00Z"
}
```

`description`, `priority`, and `dueAt` are optional. Title is required (max
200 characters); description max 4000. Example response:

```json
{
  "id": 1,
  "pharmacyKey": "PHARM-001",
  "title": "Prepare refill queue",
  "description": "Review today's scheduled refills",
  "status": "TODO",
  "priority": "HIGH",
  "dueAt": "2026-10-04T09:00:00Z",
  "assigneeUserId": 5,
  "createdByUserId": 1,
  "createdAt": "2026-10-03T09:00:00Z",
  "updatedAt": "2026-10-03T09:00:00Z",
  "completedAt": null
}
```

### `GET /tasks`

Optional query parameters: `pharmacyKey`, `status`. Returns a **bare JSON
array** of task objects in the shape above, limited to the first 100 visible
tasks ordered by due date (nulls last), then newest creation time. Pagination
is not implemented yet.

### `GET /tasks/{id}`

Returns one visible task object, or `404` when absent or hidden from the user.

### `PATCH /tasks/{id}/status`

Body: `{"status":"IN_PROGRESS"}`. Returns the updated task object. Only the
task's assignee or an owner/store manager with pharmacy access can change its
status. `COMPLETED`, `SKIPPED`, and `CANCELED` are terminal; further status
changes return `409`.

### `POST /tasks/{id}/comments`

Body: `{"commentText":"Items verified"}` (max 2000 characters). Returns `201`:

```json
{"id":1,"taskId":1,"authorUserId":5,"commentText":"Items verified","createdAt":"2026-10-03T09:10:00Z"}
```

### `GET /tasks/{id}/comments`

Returns a bare array of comments in chronological order. The task must be
visible to the caller. Hard task deletion is not available in this release.

## Starter dashboards

These endpoints require the existing dashboard permission as well as the
authenticated user's pharmacy scope. The tenant comes from the bearer token;
there is no client-supplied tenant ID or schema name.

| Endpoint | Required permission | Scope |
| --- | --- | --- |
| `GET /dashboards/owner` | `dashboard.owner.read` | Active pharmacies in the tenant |
| `GET /dashboards/store-manager` | `dashboard.store_manager.read` | Explicitly assigned active pharmacies |

Both accept optional `startDate` and `endDate` (`YYYY-MM-DD`, inclusive) and
`pharmacyKey`. With no dates, the range is the latest 30 days including the
current UTC date. Explicit dates are recommended for pharmacy-local reporting
until pharmacy time zones are configured.
The maximum range is 366 days. A selected pharmacy outside the caller's scope
returns `404`; an invalid range returns `400`.

```text
GET /api/v1/dashboards/owner?startDate=2026-10-01&endDate=2026-10-05
GET /api/v1/dashboards/store-manager?pharmacyKey=PHARM-001
```

The response has the same shape for both roles:

No-data example (reflects current dev data availability; store list shortened):

```json
{
  "dashboardId": "owner",
  "generatedAt": "2026-10-05T12:00:00Z",
  "filters": {"startDate":"2026-10-01","endDate":"2026-10-05","pharmacyKey":null},
  "dataStatus": "NO_DATA",
  "latestBusinessDate": null,
  "latestImportedAt": null,
  "totals": {
    "eventCount": null,
    "billedEventCount": null,
    "rejectedEventCount": null,
    "reversedEventCount": null,
    "openTaskCount": 2,
    "overdueTaskCount": 0
  },
  "stores": [{
    "pharmacyKey": "PHARM-001", "displayName": "Demo Pharmacy",
    "dataStatus": "NO_DATA",
    "metrics": {
      "eventCount": null, "billedEventCount": null,
      "rejectedEventCount": null, "reversedEventCount": null,
      "openTaskCount": 2, "overdueTaskCount": 0
    }
  }],
  "trend": []
}
```

Illustrative response **after events are available** (synthetic numbers, not
live pharmacy data):

```json
{
  "dashboardId": "owner",
  "generatedAt": "2026-10-05T16:00:00Z",
  "filters": {"startDate":"2026-10-03","endDate":"2026-10-05","pharmacyKey":null},
  "dataStatus": "AVAILABLE",
  "latestBusinessDate": "2026-10-05",
  "latestImportedAt": "2026-10-05T15:40:00Z",
  "totals": {
    "eventCount": 12, "billedEventCount": 4,
    "rejectedEventCount": 2, "reversedEventCount": 1,
    "openTaskCount": 3, "overdueTaskCount": 1
  },
  "stores": [
    {
      "pharmacyKey": "PHARM-001", "displayName": "Demo Pharmacy 001",
      "dataStatus": "AVAILABLE",
      "metrics": {
        "eventCount": 8, "billedEventCount": 3,
        "rejectedEventCount": 1, "reversedEventCount": 1,
        "openTaskCount": 2, "overdueTaskCount": 1
      }
    },
    {
      "pharmacyKey": "PHARM-002", "displayName": "Demo Pharmacy 002",
      "dataStatus": "AVAILABLE",
      "metrics": {
        "eventCount": 4, "billedEventCount": 1,
        "rejectedEventCount": 1, "reversedEventCount": 0,
        "openTaskCount": 1, "overdueTaskCount": 0
      }
    }
  ],
  "trend": [
    {"date":"2026-10-03","eventCount":3},
    {"date":"2026-10-04","eventCount":4},
    {"date":"2026-10-05","eventCount":5}
  ]
}
```

UI placement: `totals` supplies summary cards, `stores` supplies the comparison
table, and `trend` supplies a daily event-activity chart. Show `dataStatus`
and freshness next to those views. A store-manager response has the same shape
with `dashboardId: "store-manager"` and only assigned pharmacies; a selected
`pharmacyKey` narrows `stores`, `totals`, and `trend` to that store. Do not
derive prescription counts or money values from these event counts.

`dataStatus` is `NO_DATA`, `PARTIAL`, or `AVAILABLE`. `PARTIAL` means at least
one included pharmacy has no event data in the period or some events have a
non-`valid` data-quality status. Task counts are independent of event-data
availability. Activity fields are `null` when no usable period data exists,
not zero. `trend` contains `{date, eventCount}` points for dates with events.

**These are event counts, not unique prescription counts.** A fill can produce
several webhook events. `billedEventCount` is not revenue or cash collected;
it counts `claim_billed` events. Rejection and reversal counts likewise count
their event types. The response does not yet contain billed amount, gross
profit, net profit, patient metrics, or payer/drug profitability. Those need
validated fill-level and financial rules before publication.

## Not implemented yet

Remaining role dashboards, tenant admin, internal admin, automation, reporting, and task
assignee lookup endpoints in the broader draft are planned contracts, not
deployable APIs. Task creation currently needs a known `assigneeUserId`; the UI
cannot yet populate a picker from this service. Align each new route with the
backend team before wiring a screen to it.
