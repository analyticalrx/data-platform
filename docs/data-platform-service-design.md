# Data Platform service design

The maintained design is in the shared documentation repository:
[Data Platform Service Design](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/data-platform-service-design.md).

Use these local documents for working with this implementation:

- [Run the backend](../README.md#local-setup)
- [Implemented UI API contract](ui-api-contract.md)
- [Backend documentation index](README.md)
- [Applied bigint ID migration](../database/bigint-id-migration-plan.md)

The shared repository also owns the [service diagram](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/diagrams/data-platform-service.svg)
and its [editable source](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/diagrams/data-platform-service.d2).

## Historical draft

The [former backend draft](https://github.com/analyticalrx/data-platform/blob/f8dd98ad59759fb94c6fed51840cec96d4273c1d/docs/data-platform-service-design.md)
is preserved in Git history. It describes older multi-tenant JWT claims,
`user_tenant_access`/`user_role_assignment` tables, and role names such as
`OWNER_EXECUTIVE` and `BOOKKEEPER_FINANCE`. These are historical proposals;
consult the [RBAC migration](../database/manual-migrations/2026-09-09-simplified-user-rbac.sql),
[ID migration](../database/bigint-id-migration-plan.md), and implemented API contract
for the current model. This file remains as a pointer for existing links.
