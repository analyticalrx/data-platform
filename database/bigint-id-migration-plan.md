# Bigint ID Migration Plan

Status: applied to the Phase 1 dev database on 2026-09-29 using
`manual-migrations/2026-09-29-bigint-internal-ids.sql`.

## Decision

Use PostgreSQL `bigint generated ... as identity` for every **internal** row ID. All foreign keys to those rows must also be `bigint`. Use Java `long`/`Long` for those IDs (`Long` where the value can be absent). A composite primary key can remain composite if each ID component is `bigint`.

Keep vendor and correlation identifiers in their original formats: PrimeRx Rx numbers, refill numbers, NDCs, source pharmacy/payer/drug IDs, webhook message IDs, request IDs, and opaque reference tokens remain `text`. The JWT `jti` may remain a random string; it is not a database row ID. Do not expose sequential IDs as a substitute for tenant authorization.

## Dev database before migration (2026-09-29)

The database had one tenant, five application users, nine roles, 15 permissions, five user-role assignments, and five raw payload archive rows. These rows were preserved.

| Schema | Already bigint | Must convert to bigint |
| --- | --- | --- |
| `control_plane` | `app_user.id`, `user_pharmacy_access.id`, audit row IDs and user FKs | `tenant_registry.id`; every `tenant_id` FK; `role_definition.id`, `permission_definition.id`, `user_role.role_id`, both `role_permission` FKs |
| `tenant_analyticalrx_dev` | None of the row IDs | `tenant`, `pharmacy`, `drug`, `payer`, `raw_payload_archive`, `source_rx_reference`, `ingestion_event`, `pharmacy_event`, `event_payment` IDs and every internal FK, including `ingestion_event.duplicate_of` |

The `control_plane` and tenant-schema `tenant` rows represent the same business tenant. Assign them the **same numeric ID** during backfill, matched by `tenant_key`; do not independently generate two tenant IDs. Other table IDs need only be unique within their own table. The existing tenant-specific schema remains the isolation boundary.

## Migration sequence for other environments

1. Take a database snapshot and pause the processor, API, and other writers. Capture exact row counts and current constraints/indexes, and verify that each tenant schema maps to exactly one `control_plane.tenant_registry` row by `tenant_key`. The dev run used a successful rolled-back dry run and transactional commit; it did not take an RDS snapshot or pause writers.
2. In one transaction per coordinated deployment, add new bigint identity columns to parent tables and bigint replacement FK columns to child tables. Backfill parent IDs, then child FKs using the old UUID relationships. Preserve an old-to-new mapping until validation is complete.
3. Recreate primary keys, foreign keys, unique constraints, and indexes against bigint columns. Verify no dangling FKs, no null replacements for required IDs, and unchanged row counts. Replace defaults with identity generation. Keep the old UUID values only during rollback validation; remove them in a later cleanup after the new code is stable.
4. Update the source schema definitions and diagrams in `analytical-rx-documentation`, plus all application DTOs, JDBC row mappings, JWT tenant claim handling, query parameters, and processor writes/reads that use these IDs. Change the unfinished `AuthUserRepository` implementation together with this migration; it currently anticipates a `Long` tenant ID while the database still returns UUID.
5. Deploy app and processor code with the database change, then verify login, tenant lookup, role lookup, pharmacy-scoped access, raw payload insert/deduplication, and canonical event insert. Only then resume webhook traffic.

Do not run a direct `ALTER COLUMN uuid TYPE bigint`: UUID values cannot be cast meaningfully to bigint. Do not wipe the dev schema or drop existing rows to simplify this migration.

## Dev migration result

The full script passed a rolled-back dry run and then committed. It converted 39 columns and restored 85 constraints and 30 secondary indexes. Post-commit checks found zero remaining internal UUID/`smallint` IDs, one matching tenant ID across schemas, five users, five raw payload archive rows, and zero invalid foreign keys. Login and processor behavior have **not** been smoke-tested after migration; dependent code may still expect UUIDs.

## Acceptance checks

- Every internal `id`, `tenant_id`, `role_id`, `permission_id`, `pharmacy_id`, `drug_id`, `payer_id`, event FK, and `duplicate_of` column is `bigint`; source identifiers remain `text`.
- Every converted FK points to an existing parent; existing row counts and tenant assignments are unchanged.
- New inserts receive generated bigint IDs without application-generated UUIDs.
- Login and JWT validation resolve the correct tenant and roles; cross-tenant reads remain denied.
- The processor can save raw payloads and subsequent event records after the cutover.

## Notes

The older `schemas/control_plane_schema.sql` in the documentation repository predates the applied one-tenant-per-user RBAC migration. Do not use it as the live schema or mechanically convert it. Inspect the deployed schema when writing the executable migration.
