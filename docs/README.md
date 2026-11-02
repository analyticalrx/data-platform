# Backend documentation

Start with the [repository README](../README.md#local-setup) to run the backend.
Shared setup and cross-service knowledge are cataloged in the
[central documentation map](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/documentation-map.md).

## Maintained with this service

| Need | Document |
| --- | --- |
| Install, configure, start, or troubleshoot the backend | [Backend README](../README.md) |
| Integrate with currently implemented APIs | [Implemented UI API contract](ui-api-contract.md) |
| Understand the applied ID conversion | [Bigint ID migration](../database/bigint-id-migration-plan.md) |
| Inspect service-specific migrations | [Manual migrations](../database/manual-migrations/) |

## Shared references

- [Data Platform service design](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/data-platform-service-design.md)
- [Developer database/SSM access](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/operations/teammate-rds-pgadmin-access-guide.md)
- [Product requirements](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/prd/product-requirements-document.md)
- [RBAC standard](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/role-based-access-control-standard.md)
- [Planned UI API design](https://github.com/analyticalrx/analytical-rx-documentation/blob/main/architecture/ui-api-contracts.md)

## Historical reference

[Spring Boot progress tracker](spring-boot-data-platform-progress.md) is a
September 3, 2026 snapshot. Use the code, tests, and live GitHub issues for current
behavior and work status. The [former design path](data-platform-service-design.md)
now links to the maintained shared design and preserves the older version through Git history.

Update run instructions, service configuration, implemented contracts, and migrations
here alongside code changes. Update shared AWS setup, architecture, and standards
in the central repository and link to them rather than copying the instructions.
