$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot

function Import-DotEnv([string]$path) {
    if (-not (Test-Path -LiteralPath $path)) { return }
    foreach ($line in Get-Content -LiteralPath $path) {
        if ($line -match '^\s*(?:export\s+)?([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$') {
            $name = $matches[1]
            $value = $matches[2]
            if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
                Set-Item -Path "Env:$name" -Value $value
            }
        }
    }
}

Import-DotEnv (Join-Path $repoRoot '.env')

$missingDatabase = @('DATABASE_URL', 'DATABASE_USERNAME', 'DATABASE_PASSWORD') | Where-Object { [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_)) }
if ($missingDatabase.Count -gt 0) {
    throw "MySQL configuration is missing: $($missingDatabase -join ', '). Set these variables before starting the backend."
}
$missingSecrets = @('JWT_SECRET', 'AI_INTERNAL_TOKEN') | Where-Object { [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_)) }
if ($missingSecrets.Count -gt 0) {
    throw "Required secrets are missing: $($missingSecrets -join ', '). Set them before starting the backend."
}
$env:PANELS_CSV_PATH = Join-Path $repoRoot 'data\solar_panels_india.csv'
$env:LOCATIONS_CSV_PATH = Join-Path $repoRoot 'data\punjab_delhi_ncr_coordinates.csv'

$maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $maven) {
    $fallback = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.1.1\plugins\maven\lib\maven3\bin\mvn.cmd'
    if (Test-Path $fallback) { $maven = @{ Source = $fallback } }
}
if (-not $maven) { throw 'Maven was not found. Install Maven 3.9+ or run this from IntelliJ IDEA.' }

Push-Location $repoRoot
try {
    & $maven.Source -f "$repoRoot\backend\pom.xml" spring-boot:run '-Dspring-boot.run.profiles=local'
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
finally {
    Pop-Location
}
