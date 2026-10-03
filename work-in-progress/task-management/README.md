# Task management (isolated)

This code is parked outside `src/main/java`. It is not compiled, component-scanned,
or exposed by the data-platform application. The current implementation does not
build against the project's JJWT version and has not been integrated with the
existing authentication and tenant-access model.

Before moving it back, reconcile its JWT/security configuration with the main
application, make it compile, and verify its endpoints and data access.
