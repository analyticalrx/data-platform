# Spring Boot Data Platform Progress Tracker

Created: September 3, 2026

Status: Draft for backend implementation review

Primary audience: backend, frontend, data engineering, and implementation leads

> **Historical snapshot:** This tracker was created on September 3, 2026. The service has continued to evolve since then, so its progress table is not current. Use the code, tests, and live GitHub issues for present implementation status.

## 1. Purpose

This document tracks how we will build the AnalyticalRx Phase 1 Data Platform Service as a Java 21 Spring Boot application backed by PostgreSQL.

The service will sit between the frontend and the processed/canonical database. It will not receive PrimeRx webhooks directly. Its job is to authenticate users, enforce role-based access, resolve tenant schema access safely, and expose dashboard/admin APIs from canonical processed data.

Source documents:

- [Data Platform Service Design](data-platform-service-design.md)
- [UI API Contracts](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/ui-api-contracts.md)
- [RBAC Role Summary](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/rbac-role-summary.md)
- [RBAC Standard](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/role-based-access-control-standard.md)
- [MVP Canonical Event SQL](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/schemas/mvp_canonical_event_schema.sql)
- [Control Plane SQL](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/schemas/control_plane_schema.sql)

## 2. Current Progress Snapshot

| Area | Status                       | Notes |
| --- |------------------------------| --- |
| API contract | Review ready                 | `architecture/ui-api-contracts.md` defines auth, `/me`, tenant/pharmacy lookup, dashboards, tenant admin, and internal admin APIs. |
| Dashboard mockups | Review ready                 | Compact SVG mockups exist under `diagrams/dashboard-mockups/` and are referenced by the API contract. |
| PostgreSQL dev database | Deployed in AWS              | RDS PostgreSQL, RDS Proxy, SSM local access, `control_plane`, and `tenant_analyticalrx_dev` are available. |
| Canonical tenant schema | Dev database deployed in AWS | Tenant schema contains `tenant`, `pharmacy`, `drug`, `payer`, `raw_payload_archive`, `source_rx_reference`, `ingestion_event`, `pharmacy_event`, and `event_payment`. |
| Control plane schema | Dev Database deployed in AWS | Shared non-PHI app metadata tables exist for users, tenants, roles, assignments, and audit. |
| Spring Boot service code | In progress                  | This is the next implementation step. |
| JWT signing secret | Not started                  | Need a Secrets Manager entry or local dev secret for signing access tokens. |
| Dashboard read models | Not started                  | API contract exists; SQL/services need to be implemented. |
| Deployment pipeline | Not started                  | Container build and AWS deployment can follow after local vertical slice works. |

## 3. Locked Phase 1 Decisions

| Decision | Current direction |
| --- | --- |
| Language/runtime | Java 21. |
| Backend framework | Spring Boot 3.5.16. |
| Database | PostgreSQL on AWS RDS through RDS Proxy. |
| Tenant isolation | One PostgreSQL schema per tenant for tenant-owned pharmacy/event data. |
| Shared app metadata | `control_plane` schema for users, roles, tenant registry, schema mapping, and app audit. |
| Authentication | Username/password login returning JWT bearer access token. |
| Authorization | Backend-enforced tenant, role, and pharmacy-level RBAC. |
| Dashboard data source | Processed canonical tables, not raw PrimeRx payloads. |
| Restricted fields | Normal UI APIs must not return PHI, raw payloads, raw API responses, real `RxNo`, or real `RefillNo`. |

## 4. Service Boundary

### This Service Owns

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `GET /api/v1/me`
- tenant and pharmacy lookup APIs
- Phase 1 role dashboard APIs
- tenant admin APIs
- internal platform admin APIs
- JWT issuing and validation
- tenant schema resolution
- RBAC enforcement
- dashboard-safe read models
- app access and permission audit records

### This Service Does Not Own

- PrimeRx webhook receiving
- API Gateway receiver Lambda
- SQS queue processing
- webhook processor Lambda persistence
- PrimeRx SFTP ingestion jobs
- raw payload object storage
- Doctor CRM workflows
- refill automation workflows
- advanced analytics or ML forecasting

Those areas can integrate with this service later, but they are not required for the first Spring Boot vertical slice.

## 5. Recommended Spring Boot Structure

Use one application with clear packages before introducing additional services.

```text
com.analyticalrx.platform
  AnalyticalRxPlatformApplication
  config
    SecurityConfig
    DatabaseConfig
    WebConfig
  common
    ApiError
    ApiExceptionHandler
    RequestIdFilter
    PageRequest
    DateRange
  auth
    AuthController
    AuthService
    PasswordService
    JwtService
    CurrentUser
    CurrentUserService
  tenancy
    TenantController
    TenantResolver
    TenantSchemaResolver
    PharmacyAccessService
  rbac
    RoleCode
    PermissionCode
    AccessEvaluator
    RequirePermission
  dashboard
    DashboardController
    DashboardResponse
    DashboardCard
    DashboardChart
    DashboardTable
    OwnerDashboardService
    StoreManagerDashboardService
    PharmacistDashboardService
    TechnicianDashboardService
    BookkeeperDashboardService
    ComplianceDashboardService
    BusinessDevelopmentDashboardService
  canonical
    CanonicalQueryRepository
    DashboardSqlBuilder
    SafeSchemaName
  admin
    TenantAdminController
    InternalAdminController
    UserAdminService
    RoleAssignmentService
  audit
    AppAccessAuditService
    PermissionAuditService
  database
    ControlPlaneRepository
    TenantSchemaRepository
```

## 6. Initial Dependencies

Use Maven with a checked-in wrapper, Java 21, and Spring Boot 3.5.16. Replace the
locally generated `4.2.0-SNAPSHOT` parent before the service skeleton is committed.

Recommended dependencies:

| Dependency | Purpose |
| --- | --- |
| `spring-boot-starter-web` | REST APIs. |
| `spring-boot-starter-security` | Authentication and authorization pipeline. |
| `spring-boot-starter-oauth2-resource-server` | JWT bearer token validation support. |
| `spring-security-oauth2-jose` | JWT signing/verification primitives. |
| `spring-boot-starter-validation` | Request validation. |
| `spring-boot-starter-data-jpa` | Default entity mapping, repositories, transactions, and standard persistence. |
| PostgreSQL JDBC driver | PostgreSQL/RDS connectivity. |
| `spring-boot-starter-actuator` | Health checks and operational readiness. |
| Flyway or Liquibase | Database migration management. |
| Testcontainers PostgreSQL | Integration tests against real PostgreSQL behavior. |

Spring Data JPA is the default persistence mechanism. Use JPA entities and
repositories for control-plane, pharmacy, audit, administration, and future
workflow records. Use DTO projections for straightforward dashboard summaries.
Use targeted native SQL only where conditional aggregates, PostgreSQL-specific
features, or complex grouping are materially clearer than JPQL. Consider jOOQ
only when reporting-query volume justifies code generation.

JPA rules:

- Set `spring.jpa.open-in-view=false`.
- Return API DTOs rather than persistence entities.
- Avoid eager relationships and unbounded entity graphs.
- Do not model cross-schema relationships without database foreign keys.
- Use Flyway for schema changes and disable Hibernate schema updates in deployed
  environments.

## 7. Runtime Configuration

Initial environment variables:

| Variable | Required | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Yes | Runtime location: `local` or `aws`. |
| `APP_ENVIRONMENT` | Yes | Resource environment: `dev`, `stage`, or `prod`. |
| `AWS_REGION` | Yes | Current Phase 1 value: `us-east-1`. |
| `DB_SECRET_ID` | Yes | Secrets Manager secret containing the database credentials and RDS Proxy endpoint. |
| `DB_HOST_OVERRIDE` | Local only | Use `localhost` while the SSM tunnel is active. |
| `DB_PORT_OVERRIDE` | Local only | Current tunnel port: `5433`. |
| `DB_SSL_MODE` | Yes | Start with `require`; test certificate verification before production. |
| `DB_POOL_MAX_SIZE` | Yes | Hikari connections per instance. Start with `5`. |
| `DB_CONNECTION_TIMEOUT_MS` | Yes | Start with `5000`. |
| `CONTROL_PLANE_SCHEMA` | Yes | Current value: `control_plane`. |
| `JWT_ISSUER` | Yes | Example: `analyticalrx-api`. |
| `JWT_AUDIENCE` | Yes | Example: `analyticalrx-frontend`. |
| `JWT_SIGNING_SECRET_ID` | AWS only | Secrets Manager secret for JWT signing key. |
| `JWT_SIGNING_SECRET` | Local only | Local dev signing secret. Do not commit this. |
| `JWT_ACCESS_TOKEN_TTL_MINUTES` | Yes | Start with 15 to 30 minutes. |

Local database path:

```text
Spring Boot local app
  -> localhost:5433
  -> SSM tunnel
  -> RDS Proxy
  -> RDS PostgreSQL
```

Deployed database path:

```text
Spring Boot container
  -> RDS Proxy endpoint:5432
  -> RDS PostgreSQL
```

### Credential Resolution

The application retrieves the database secret at startup using the AWS SDK
default credential provider chain.

- Local: use the developer's configured AWS CLI identity, then override the
  secret host and port with `localhost:5433`.
- ECS Fargate: use the task role.
- EC2: use the instance profile.
- Never place long-lived AWS access keys in the container configuration.
- Never log the secret value, database password, or complete JDBC URL.

For the first checkpoint, secret rotation requires an application restart or ECS
service redeployment. Automatic credential refresh can follow after connectivity
is proven and a rotation schedule is defined.

### M1 Database Connection Definition Of Done

- Local startup retrieves `analytical-rx/phase1/dev/postgres/app-rw` and
  connects through the SSM tunnel.
- AWS startup uses a task role or instance profile and connects directly to RDS
  Proxy.
- The same application artifact is used in both environments.
- Readiness health includes database connectivity without exposing connection
  details.
- A verification query confirms the expected database user, database,
  `control_plane`, and dev tenant schema.
- Missing or invalid configuration fails startup with a sanitized message.
- Unit tests cover secret parsing and local host overrides.
- A PostgreSQL Testcontainers test verifies JPA connectivity.

## 8. Authentication Design

### Login Flow

1. User calls `POST /api/v1/auth/login` with email/username and password.
2. Service finds user in `control_plane.app_user`.
3. Service verifies `password_hash` using the configured password algorithm, initially BCrypt.
4. Service loads active tenant access and role assignments.
5. Service issues a signed JWT access token.
6. Service writes an `app_access_audit_event` row.
7. Frontend calls protected APIs using:

   ```text
   Authorization: Bearer <access_token>
   ```

### JWT Claims

JWT should include:

- `sub` user ID
- `email`
- `display_name`
- `tenant_access`
- role codes
- allowed pharmacy scope
- internal roles where applicable
- `permissions_version`
- issuer, audience, issue time, expiry, and token ID

JWT must not include:

- database schema name
- PHI
- real `RxNo`
- real `RefillNo`
- raw payload reference
- patient identifiers
- insurance member identifiers
- raw vendor payload data

### Role Change Handling

Use `control_plane.app_user.permissions_version`.

When a user role, tenant access, pharmacy scope, or user status changes:

1. Increment `permissions_version`.
2. Write a `permission_audit_event`.
3. Reject stale tokens for sensitive APIs or force the frontend to refresh user context.

## 9. Tenant And Schema Resolution

The API path uses a public tenant key:

```text
/api/v1/tenants/{tenantKey}/dashboards/owner
```

The service must never trust a schema name from the client or JWT.

Request handling should be:

1. Validate JWT.
2. Confirm `tenantKey` is assigned to the current user.
3. Load tenant metadata from `control_plane.tenant_registry`.
4. Validate the stored schema name against the approved pattern: `tenant_[a-z0-9_]+`.
5. Use that schema only for tenant-owned dashboard queries.
6. Apply pharmacy scope before returning data.

Do not set a permanent `search_path` on pooled connections. For Phase 1, prefer schema-qualified SQL built from the trusted schema name:

```sql
select count(*)
from tenant_analyticalrx_dev.pharmacy_event
where business_date between :startDate and :endDate
```

In code, only the schema identifier can be inserted into the SQL string, and only after it was loaded from `control_plane.tenant_registry` and validated. All values such as dates, IDs, filters, and search text must be bound parameters.

## 10. RBAC Design

Use default deny. Every protected endpoint must check:

- valid JWT
- active user
- active tenant access
- required role/permission
- allowed pharmacy scope
- restricted field rules

Initial permission mapping:

| API area | Required role(s) |
| --- | --- |
| Owner dashboard | `OWNER_EXECUTIVE` |
| Store manager dashboard | `STORE_MANAGER`, `OWNER_EXECUTIVE` |
| Pharmacist dashboard | `PHARMACIST`, `STORE_MANAGER`, `OWNER_EXECUTIVE` |
| Technician dashboard | `TECHNICIAN`, `PHARMACIST`, `STORE_MANAGER` |
| Bookkeeper dashboard | `BOOKKEEPER_FINANCE`, `OWNER_EXECUTIVE` |
| Compliance/admin dashboard | `COMPLIANCE_ADMIN`, `TENANT_ADMIN`, `OWNER_EXECUTIVE` |
| Business development dashboard | `BUSINESS_DEVELOPMENT`, `STORE_MANAGER`, `OWNER_EXECUTIVE` |
| Tenant admin APIs | `TENANT_ADMIN` |
| Internal admin APIs | `INTERNAL_PLATFORM_ADMIN` |

`TENANT_ADMIN` does not automatically mean owner, finance, pharmacist, or business development dashboard access. Add business roles separately when that user also needs those dashboards.

## 11. Dashboard API Implementation

All dashboard endpoints should return the shared response shape from [UI API Contracts](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/ui-api-contracts.md).

Shared backend DTOs:

```text
DashboardResponse
  dashboardId
  title
  tenantKey
  generatedAt
  filters
  dataFreshness
  cards[]
  charts[]
  tables[]
  warnings[]
```

Start with one dashboard API, then reuse the same model for the rest.

Recommended first dashboard:

```text
GET /api/v1/tenants/{tenantKey}/dashboards/owner
```

Minimum first response:

- `generatedAt`
- request filters
- data freshness from `ingestion_event`
- prescription/event count from `pharmacy_event`
- amount totals from `pharmacy_event` and/or `event_payment` where available
- store comparison from `pharmacy`
- warning if financial data is partial

The API should return empty/partial states instead of failing when source data is missing.

## 12. Suggested SQL Read Models

### Data Freshness

```sql
select max(received_at) as latest_event_at
from {tenant_schema}.ingestion_event
where status in ('received', 'queued', 'processing', 'processed', 'failed', 'needs_enrichment');
```

### Owner Event Count

```sql
select count(*) as event_count
from {tenant_schema}.pharmacy_event pe
where pe.business_date between :startDate and :endDate
  and (:pharmacyIdsEmpty = true or pe.pharmacy_id = any(:pharmacyIds));
```

### Event Trend

```sql
select pe.business_date as date, count(*) as count
from {tenant_schema}.pharmacy_event pe
where pe.business_date between :startDate and :endDate
  and (:pharmacyIdsEmpty = true or pe.pharmacy_id = any(:pharmacyIds))
group by pe.business_date
order by pe.business_date;
```

### Store Comparison

```sql
select
  p.id as pharmacy_id,
  p.display_name as pharmacy_name,
  count(pe.id) as prescription_events,
  coalesce(sum(pe.total_amount), 0) as total_amount,
  count(*) filter (where pe.claim_status = 'rejected' or pe.canonical_event_type = 'claim_rejected') as rejected_claims
from {tenant_schema}.pharmacy p
left join {tenant_schema}.pharmacy_event pe
  on pe.pharmacy_id = p.id
 and pe.business_date between :startDate and :endDate
where (:pharmacyIdsEmpty = true or p.id = any(:pharmacyIds))
group by p.id, p.display_name
order by p.display_name;
```

### Payer Summary

```sql
select
  pay.payer_id,
  payer.payer_name,
  coalesce(sum(pay.billed_amount), 0) as billed_amount,
  coalesce(sum(pay.paid_amount), 0) as paid_amount,
  count(*) as event_count
from {tenant_schema}.event_payment pay
left join {tenant_schema}.payer payer
  on payer.id = pay.payer_id
join {tenant_schema}.pharmacy_event pe
  on pe.id = pay.pharmacy_event_id
where pe.business_date between :startDate and :endDate
  and (:pharmacyIdsEmpty = true or pe.pharmacy_id = any(:pharmacyIds))
group by pay.payer_id, payer.payer_name
order by paid_amount desc;
```

## 13. First Vertical Slice

Build this before trying to implement every dashboard:

```text
Spring Boot app starts
  -> health endpoint works
  -> DB connection works through RDS Proxy
  -> login works with seeded control_plane user
  -> JWT bearer token is returned
  -> /api/v1/me returns tenant/role/pharmacy context
  -> owner dashboard endpoint resolves tenant schema
  -> owner dashboard reads canonical event/payment data
  -> RBAC denies an unauthorized dashboard request
```

Definition of done:

- Local app starts with Java 21.
- `/actuator/health` returns healthy.
- App can connect to `analyticalrx` through local SSM tunnel.
- At least one dev user can log in.
- JWT contains tenant-scoped role claims but no schema name or PHI.
- `/api/v1/me` matches the API contract.
- `/api/v1/tenants/{tenantKey}/pharmacies` returns only allowed pharmacies.
- Owner dashboard returns the common dashboard response shape.
- Unauthorized role access returns `403`.
- Invalid tenant access returns `403` or safe `404`.
- Logs contain request IDs but no PHI or tokens.

## 14. Implementation Milestones

| Milestone | Status | Target outcome |
| --- | --- | --- |
| M0: Project skeleton | Not started | Spring Boot Java 21 project compiles and exposes health endpoint. |
| M1: Database connection | Planned | Same artifact connects locally through SSM and in AWS through RDS Proxy using Secrets Manager credentials. |
| M2: Control plane access | Not started | Service can read users, tenant registry, roles, and assignments. |
| M3: Auth and JWT | Not started | Login returns signed JWT and `/me` works. |
| M4: Tenant and RBAC guard | Not started | Tenant schema is resolved safely and role/pharmacy scope is enforced. |
| M5: Owner dashboard slice | Not started | First dashboard reads canonical data and returns common contract shape. |
| M6: Remaining starter dashboards | Not started | All Phase 1 dashboard endpoints exist with safe partial/empty states. |
| M7: Tenant admin APIs | Not started | Tenant admin can manage allowed users/roles/settings. |
| M8: Internal admin APIs | Not started | Internal admin can view tenants, users, role assignments, and pipeline status. |
| M9: Tests and hardening | Not started | RBAC, tenant isolation, restricted-field, and database tests are automated. |
| M10: Container deployment | Not started | Service builds as container and deploys to AWS dev. |

## 15. Component Build Checklist

| Component | Status | Notes |
| --- | --- | --- |
| Java 21 project setup | Not started | Add wrapper, build file, base package, formatting rules. |
| Health endpoint | Not started | Use Spring Actuator. |
| Common error response | Not started | Match API contract error shape. |
| Request ID logging | Not started | Generate or propagate request ID on every request. |
| DB connection pool | Planned | Hikari through RDS Proxy with environment-controlled SSL, limits, and timeouts. |
| Secrets loading | Planned | Fetch the database secret through the AWS default credential chain locally and in AWS. |
| JPA persistence baseline | Planned | JPA entities/repositories by default; DTO projections and targeted native SQL for reporting. |
| Control plane repository | Not started | Read `app_user`, `tenant_registry`, role assignments, and audit tables. |
| Password verification | Not started | Use BCrypt for Phase 1. |
| JWT issuing | Not started | Include tenant/role/pharmacy claims and `permissions_version`. |
| JWT validation | Not started | Validate issuer, audience, expiry, signature, and token status. |
| Current user context | Not started | Build server-side context from JWT plus control plane as needed. |
| Tenant schema resolver | Not started | Resolve schema from `control_plane.tenant_registry`; validate pattern. |
| RBAC evaluator | Not started | Default deny; enforce role plus pharmacy scope. |
| Dashboard DTOs | Not started | Shared response objects from API contract. |
| Owner dashboard read model | Not started | First canonical data query implementation. |
| Store manager dashboard | Not started | Can follow owner pattern. |
| Pharmacist dashboard | Not started | Starter status counts only in Phase 1. |
| Technician dashboard | Not started | Starter status counts only in Phase 1. |
| Bookkeeper dashboard | Not started | Use `event_payment`, `payer`, and safe financial fields. |
| Compliance/admin dashboard | Not started | Use ingestion and audit summaries; no raw payload body. |
| Business development dashboard | Not started | Safe prescriber trend only where source data supports it. |
| Tenant admin user APIs | Not started | Manage customer users and role assignments inside tenant scope. |
| Internal admin APIs | Not started | Cross-tenant admin tool inside same core service. |
| App access audit writer | Not started | Log login/logout/token/admin/sensitive-denied events. |
| Permission audit writer | Not started | Log role and scope changes. |
| Unit tests | Not started | Auth, JWT, tenant resolver, RBAC, DTO mapping. |
| Integration tests | Not started | PostgreSQL/Testcontainers and endpoint tests. |
| Container image | Not started | Java 21 runtime image. |
| AWS dev deployment | Not started | ECS/Fargate or equivalent container runtime. |

## 16. API Contract Review Notes

The current API contract is aligned enough to begin backend implementation.

Build order from the contract:

1. `POST /api/v1/auth/login`
2. `GET /api/v1/me`
3. `GET /api/v1/tenants`
4. `GET /api/v1/tenants/{tenantKey}/pharmacies`
5. `GET /api/v1/tenants/{tenantKey}/dashboards/owner`
6. Remaining dashboard APIs
7. Tenant admin APIs
8. Internal admin APIs

Important implementation rules from the contract:

- All authenticated endpoints require bearer token auth.
- `tenantKey` is public routing context, not the database schema name.
- The backend must intersect requested `pharmacyIds` with the user's allowed pharmacy scope.
- Dashboard APIs must support date filters.
- Dashboard APIs must return common `cards`, `charts`, `tables`, and `warnings` structures.
- Missing or incomplete source data should return `partial` or `unavailable` states, not broken responses.
- Normal UI APIs must not return raw source payloads, raw source responses, real `RxNo`, real `RefillNo`, or direct patient identifiers.

## 17. Test Plan

### Unit Tests

- password hash verification
- JWT creation and validation
- expired token rejection
- permissions version handling
- tenant schema name validation
- tenant access check
- pharmacy scope filtering
- dashboard response DTO mapping
- common error response mapping

### Integration Tests

- login succeeds for active user
- login fails for inactive user
- login fails for invalid password
- `/me` returns assigned tenants and roles
- owner can access owner dashboard
- technician cannot access bookkeeper dashboard
- store manager cannot query unassigned pharmacy data
- tenant admin cannot assign `INTERNAL_PLATFORM_ADMIN`
- internal admin can access internal admin APIs
- dashboard response does not include restricted fields
- tenant schema query does not accept schema name from request input

### Database Tests

- migrations run from empty database
- seeded roles exist
- tenant registry maps `analyticalrx-dev` to `tenant_analyticalrx_dev`
- owner dashboard query works with no events
- owner dashboard query works with sample events
- cash payment rows do not require `payer_id`
- payer summary handles `payer_id = null`

## 18. Do Not Build Yet

Keep the first implementation narrow.

- Do not build Doctor CRM workflows in Phase 1 service slice.
- Do not build refill queue automation in the first slice.
- Do not build custom workflow/rule builder.
- Do not expose raw payload viewer in normal dashboards.
- Do not add patient-facing email or SMS.
- Do not start ML/forecasting APIs yet.
- Do not solve refresh tokens unless short-lived access tokens create a real blocker.

## 19. Open Decisions

| Decision | Current recommendation | Blocking? |
| --- | --- | --- |
| Build tool | Use Maven and check in the wrapper with the service skeleton. | No |
| JWT signing key storage | Use local env for dev, Secrets Manager for AWS. | Needed before auth implementation |
| Refresh token support | Skip for first vertical slice. | No |
| Persistence strategy | Use JPA by default, DTO projections for summaries, and targeted native SQL for complex reporting. | No |
| Database authentication | Use Secrets Manager credentials for the first checkpoint; keep IAM authentication as a compatible later option. | No |
| Migration user | Dev can use existing app user; production should use separate migration/admin user. | No for dev |
| Deployed runtime | Container service in AWS, likely ECS Fargate. | Not blocking local build |
| Initial seeded users | Need one internal admin and at least one tenant business user. | Needed before login testing |

## 20. Progress Log

| Date | Update | Owner |
| --- | --- | --- |
| 2026-09-03 | API contract reviewed and dashboard mockups added for frontend alignment. | Product/backend |
| 2026-09-03 | RDS/Postgres dev database, control plane schema, and tenant schema are available for development. | Backend/platform |
| 2026-09-03 | Spring Boot service design and progress tracker created. | Backend |
| 2026-09-04 | Planned local/AWS RDS connectivity, Secrets Manager loading, and hybrid JPA/reporting persistence. | Backend/platform |

## 21. Immediate Next Step

Create the Java 21 Spring Boot project and implement the first non-business vertical slice:

```text
health endpoint
  -> Secrets Manager credential loading
  -> local SSM / AWS RDS Proxy database connection check
  -> JPA persistence baseline
  -> common error contract
  -> request ID logging
```

After that, implement:

```text
login
  -> JWT
  -> /me
  -> tenant/pharmacy lookup
  -> owner dashboard
```

This sequence proves the service foundation before the team spends time implementing every dashboard.
