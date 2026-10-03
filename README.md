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
- [Applied bigint ID migration](database/bigint-id-migration-plan.md)

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

### Debug in VS Code (macOS/Linux)

The Spring Boot Dashboard/Java debugger starts a new process; it does not read
variables exported in a separate terminal. Its launch configuration uses the
repository-root `.env` file, which is Git-ignored.

1. Start the SSM database tunnel in a terminal and keep it open. The command is
   in [Start the SSM database tunnel](#2-start-the-ssm-database-tunnel).
2. In the repository root, run `AWS_PROFILE=your-analyticalrx-profile bash scripts/setup-vscode-env.sh`.
   The script checks the AWS account and retrieves the database and JWT secrets
   into `.env` with owner-only permissions. If you use environment credentials
   rather than a named profile, omit `AWS_PROFILE=...`. It will not overwrite an
   existing `.env`.
3. In VS Code, open this repository as the workspace. Select
   `Spring Boot-DataplatformApplication<dataplatform>` in **Run and Debug** and
   press F5, or debug the same project from Spring Boot Dashboard. Set a
   breakpoint in `AuthController.login` and send a login request.

If the launch configuration is absent, add a Java launch configuration with
`"mainClass": "com.analyticalrx.dataplatform.DataplatformApplication"` and
`"envFile": "${workspaceFolder}/.env"`. `DB_JDBC_URL` uses local port 5433, so
the SSM tunnel must still be running. Never commit `.env` or paste its secrets
into `launch.json`.

### Windows Command Prompt setup

Use two Command Prompt windows: Terminal 1 keeps the SSM database tunnel open;
Terminal 2 retrieves secrets and starts Spring Boot. Do not close Terminal 1
while the service is running.

#### 1. Create a personal AWS CLI profile

Each developer must configure their own IAM access key and secret access key.
Do not share access keys or commit them to this repository.

```cmd
aws configure --profile your-analyticalrx-profile
```

Enter `us-east-1` as the default Region and `json` as the output format. For
example, Vishnuvardhan's local profile is `vishnu-analyticalrx`.

Verify the profile before continuing:

```cmd
set "AWS_PROFILE=your-analyticalrx-profile"
set "AWS_REGION=us-east-1"
set "AWS_DEFAULT_REGION=us-east-1"
set "AWS_PAGER="
aws sts get-caller-identity --no-cli-pager
```

The returned account ID must be `209556026827`.

#### 2. Terminal 1: start the SSM database tunnel

```cmd
set "AWS_PROFILE=your-analyticalrx-profile"
set "AWS_REGION=us-east-1"
set "AWS_DEFAULT_REGION=us-east-1"
set "AWS_PAGER="

aws ssm start-session --region us-east-1 --target i-0ee64eae7d8505bfc --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters host=analytical-rx-phase1-rds-proxy.proxy-c6ry4mqo6ydb.us-east-1.rds.amazonaws.com,portNumber=5432,localPortNumber=5433
```

The tunnel maps `127.0.0.1:5433` to the RDS Proxy. It requires the AWS Session
Manager plugin to be installed locally.

#### 3. Terminal 2: retrieve secrets and run the service

```cmd
cd C:\path\to\data-platform

set "AWS_PROFILE=your-analyticalrx-profile"
set "AWS_REGION=us-east-1"
set "AWS_DEFAULT_REGION=us-east-1"
set "AWS_PAGER="

set "DB_JDBC_URL=jdbc:postgresql://127.0.0.1:5433/analyticalrx?sslmode=require"

for /f "delims=" %A in ('aws secretsmanager get-secret-value --region us-east-1 --secret-id analytical-rx/phase1/dev/postgres/data-platform-password-rw --query SecretString --output text --no-cli-pager') do set "DB_SECRET_JSON=%A"
for /f "delims=" %A in ('powershell -NoProfile -Command "$s = ConvertFrom-Json -InputObject $env:DB_SECRET_JSON; [Console]::Write($s.username)"') do set "DB_USERNAME=%A"
for /f "delims=" %A in ('powershell -NoProfile -Command "$s = ConvertFrom-Json -InputObject $env:DB_SECRET_JSON; [Console]::Write($s.password)"') do set "DB_PASSWORD=%A"
set "DB_SECRET_JSON="

for /f "delims=" %A in ('aws secretsmanager get-secret-value --region us-east-1 --secret-id analytical-rx/phase1/dev/jwt-signing-secret --query SecretString --output text --no-cli-pager') do set "JWT_SIGNING_SECRET=%A"

set "JWT_ACCESS_TOKEN_TTL_MS=1800000"
set "JWT_ISSUER=analyticalrx-api"
set "JWT_AUDIENCE=analyticalrx-frontend"

if defined DB_USERNAME (echo DB username loaded) else (echo DB username NOT loaded)
if defined DB_PASSWORD (echo DB password loaded) else (echo DB password NOT loaded)
if defined JWT_SIGNING_SECRET (echo JWT secret loaded) else (echo JWT secret NOT loaded)

.\mvnw.cmd spring-boot:run
```

Values set with `set` exist only in the current Command Prompt window. The
`for /f` commands use `%A` because they are pasted into Command Prompt; use
`%%A` if placing the same commands in a `.cmd` file. Do not print or commit
`DB_PASSWORD`, `JWT_SIGNING_SECRET`, or `DB_SECRET_JSON`.

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
  -d '{"tenantKey":"<tenant-key>","email":"<user-email>","password":"<password>"}'
```

### 5. Test pharmacy access

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
