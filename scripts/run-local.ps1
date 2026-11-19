param(
  [string]$Profile,
  [ValidateRange(1, 65535)]
  [int]$LocalPort = 5433
)

$ErrorActionPreference = "Stop"
$Region = "us-east-1"
$ExpectedAccount = "209556026827"

if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
  throw "AWS CLI is required. Install it, configure your personal AWS credentials, and try again."
}

$awsOptions = @("--region", $Region)
if ($Profile) { $awsOptions += @("--profile", $Profile) }

$identityJson = aws @awsOptions sts get-caller-identity --output json
if ($LASTEXITCODE -ne 0) { throw "Could not check AWS credentials." }
$identity = $identityJson | ConvertFrom-Json
if ($identity.Account -ne $ExpectedAccount) { throw "Wrong AWS account. Expected $ExpectedAccount, got $($identity.Account)." }
if ($identity.Arn -like "*:root") { throw "Configure a personal development IAM profile to start the backend." }

$dbJson = aws @awsOptions secretsmanager get-secret-value --secret-id analytical-rx/phase1/dev/postgres/data-platform-password-rw --query SecretString --output text
if ($LASTEXITCODE -ne 0) { throw "Could not read the database secret. Ask for Secrets Manager access." }
$db = $dbJson | ConvertFrom-Json

$jwtSecret = aws @awsOptions secretsmanager get-secret-value --secret-id analytical-rx/phase1/dev/jwt-signing-secret --query SecretString --output text
if ($LASTEXITCODE -ne 0) { throw "Could not read the JWT secret. Ask for Secrets Manager access." }

$env:DB_JDBC_URL = "jdbc:postgresql://127.0.0.1:${LocalPort}/analyticalrx?sslmode=require"
$env:DB_USERNAME = $db.username
$env:DB_PASSWORD = $db.password
$env:JWT_SIGNING_SECRET = $jwtSecret
$env:JWT_ACCESS_TOKEN_TTL_MS = "1800000"
$env:JWT_ISSUER = "analyticalrx-api"
$env:JWT_AUDIENCE = "analyticalrx-frontend"

Write-Host "Starting Data Platform. Keep the SSM tunnel open on localhost:$LocalPort."
Push-Location (Join-Path $PSScriptRoot "..")
try {
  if ((Test-Path ".\mvnw.cmd") -and (Test-Path ".\.mvn\wrapper\maven-wrapper.properties")) {
    & .\mvnw.cmd spring-boot:run
  } elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    & mvn spring-boot:run
  } else {
    throw "Maven is required because this checkout has no Maven wrapper configuration. Install Maven, reopen the terminal, and retry."
  }
  exit $LASTEXITCODE
} finally {
  Pop-Location
}
