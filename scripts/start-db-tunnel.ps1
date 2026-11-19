param(
  [Parameter(Mandatory = $true)]
  [string]$Profile,
  [ValidateRange(1, 65535)]
  [int]$LocalPort = 5433
)

$ErrorActionPreference = "Stop"

foreach ($command in @("aws", "session-manager-plugin")) {
  if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
    throw "Install $command before starting the database tunnel."
  }
}

$awsOptions = @("--region", "us-east-1", "--profile", $Profile)
$identityJson = aws @awsOptions sts get-caller-identity --output json
if ($LASTEXITCODE -ne 0) { throw "Could not check AWS credentials." }
$identity = $identityJson | ConvertFrom-Json
if ($identity.Account -ne "209556026827" -or $identity.Arn -like "*:root") {
  throw "Use a personal IAM profile for the AnalyticalRx development account (209556026827)."
}

$databaseHost = "analytical-rx-phase1-dev-db.c6ry4mqo6ydb.us-east-1.rds.amazonaws.com"
Write-Host "Opening direct RDS access through SSM on localhost:$LocalPort. Press Ctrl+C to close the tunnel."
aws @awsOptions ssm start-session `
  --target i-0ee64eae7d8505bfc `
  --document-name AWS-StartPortForwardingSessionToRemoteHost `
  --parameters "host=$databaseHost,portNumber=5432,localPortNumber=$LocalPort"
if ($LASTEXITCODE -ne 0) { throw "Could not open the SSM database tunnel." }
