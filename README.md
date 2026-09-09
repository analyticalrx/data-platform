# AnalyticalRx Data Platform Service

This repository contains the AnalyticalRx Data Platform Service design and will
hold its implementation.

Initial plan:

- Java 21
- Spring Boot 3.5.16
- PostgreSQL through RDS Proxy
- JWT bearer authentication
- tenant/pharmacy role-based access control
- Phase 1 starter dashboard APIs

Start with:

- [Data Platform Service Design](docs/data-platform-service-design.md)
- [Spring Boot Data Platform Progress Tracker](docs/spring-boot-data-platform-progress.md)

Product requirements, API contracts, database design, and RBAC standards remain
in the private
[`analytical-rx-documentation`](https://github.com/analyticalrx/analytical-rx-documentation)
repository.

## Local setup

Requirements:

- Java 21
- Maven 3.9+ (or the included Maven wrapper)
- AWS CLI with access to the Phase 1 SSM instance and Secrets Manager
- Session Manager plugin
- `jq` (macOS/Linux only)

### 1. Retrieve the JWT secret

The JWT signing secret is stored in AWS Secrets Manager under:

```text
analytical-rx/phase1/dev/jwt-signing-secret
```

The value is never committed to this repository.

On Windows PowerShell:

```powershell
$env:JWT_SIGNING_SECRET = aws secretsmanager get-secret-value `
  --region us-east-1 `
  --secret-id analytical-rx/phase1/dev/jwt-signing-secret `
  --query SecretString --output text
```

On macOS/Linux:

```bash
export JWT_SIGNING_SECRET="$(aws secretsmanager get-secret-value \
  --region us-east-1 \
  --secret-id analytical-rx/phase1/dev/jwt-signing-secret \
  --query SecretString --output text)"
```

For deployed environments, inject this Secrets Manager value as the
`JWT_SIGNING_SECRET` environment variable. Do not commit a real value to
`.env`, source code, or documentation.

### 2. Start the SSM database tunnel

Keep this command running in a separate terminal:

On Windows PowerShell:

```powershell
aws ssm start-session `
  --region us-east-1 `
  --target i-0ee64eae7d8505bfc `
  --document-name AWS-StartPortForwardingSessionToRemoteHost `
  --parameters '{"host":["analytical-rx-phase1-rds-proxy.proxy-c6ry4mqo6ydb.us-east-1.rds.amazonaws.com"],"portNumber":["5432"],"localPortNumber":["5433"]}'
```

On macOS/Linux:

```bash
aws ssm start-session \
  --region us-east-1 \
  --target i-0ee64eae7d8505bfc \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["analytical-rx-phase1-rds-proxy.proxy-c6ry4mqo6ydb.us-east-1.rds.amazonaws.com"],"portNumber":["5432"],"localPortNumber":["5433"]}'
```

The tunnel maps `127.0.0.1:5433` to the password-authenticated RDS Proxy.

### 3. Configure database credentials and run

The password-authenticated database credentials are stored in:

```text
analytical-rx/phase1/dev/postgres/data-platform-password-rw
```

On Windows PowerShell (Java 21 must be on `PATH`):

```powershell
$env:DB_JDBC_URL = 'jdbc:postgresql://127.0.0.1:5433/analyticalrx?sslmode=require'
$dbSecret = aws secretsmanager get-secret-value `
  --region us-east-1 `
  --secret-id analytical-rx/phase1/dev/postgres/data-platform-password-rw `
  --query SecretString --output text | ConvertFrom-Json
$env:DB_USERNAME = $dbSecret.username
$env:DB_PASSWORD = $dbSecret.password

mvn spring-boot:run
```

On macOS/Linux:

```bash
export DB_JDBC_URL='jdbc:postgresql://127.0.0.1:5433/analyticalrx?sslmode=require'
DB_SECRET_JSON="$(aws secretsmanager get-secret-value \
  --region us-east-1 \
  --secret-id analytical-rx/phase1/dev/postgres/data-platform-password-rw \
  --query SecretString --output text)"
export DB_USERNAME="$(printf '%s' "$DB_SECRET_JSON" | jq -r .username)"
export DB_PASSWORD="$(printf '%s' "$DB_SECRET_JSON" | jq -r .password)"

export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

mvn spring-boot:run
```

The service starts on `http://127.0.0.1:8080`.

### 4. Test login

```bash
curl -i -X POST http://127.0.0.1:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"owner@analyticalrx.dev","password":"Owner@123"}'
```
