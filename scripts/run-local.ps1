param(
  [string]$Profile
)

$ErrorActionPreference = "Stop"
$Region = "us-east-1"
$ExpectedAccount = "209556026827"

if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
  throw "AWS CLI is required. Install it, configure your personal AWS credentials, and try again."
}

if ($Profile) { $env:AWS_PROFILE = $Profile }

$account = aws sts get-caller-identity --region $Region --query Account --output text
if ($LASTEXITCODE -ne 0) { throw "Could not check AWS credentials." }
if ($account -ne $ExpectedAccount) { throw "Wrong AWS account: expected $ExpectedAccount, got $account." }

$dbJson = aws secretsmanager get-secret-value --region $Region --secret-id analytical-rx/phase1/dev/postgres/data-platform-password-rw --query SecretString --output text
if ($LASTEXITCODE -ne 0) { throw "Could not read the database secret. Ask for Secrets Manager access." }
$db = $dbJson | ConvertFrom-Json

$env:DB_JDBC_URL = "jdbc:postgresql://127.0.0.1:5433/analyticalrx?sslmode=require"
$env:DB_USERNAME = $db.username
$env:DB_PASSWORD = $db.password
$env:JWT_SIGNING_SECRET = aws secretsmanager get-secret-value --region $Region --secret-id analytical-rx/phase1/dev/jwt-signing-secret --query SecretString --output text
if ($LASTEXITCODE -ne 0) { throw "Could not read the JWT secret. Ask for Secrets Manager access." }
$env:JWT_ACCESS_TOKEN_TTL_MS = "1800000"
$env:JWT_ISSUER = "analyticalrx-api"
$env:JWT_AUDIENCE = "analyticalrx-frontend"

Write-Host "Starting the backend. Keep the database tunnel open in its other window."
& .\mvnw.cmd spring-boot:run
