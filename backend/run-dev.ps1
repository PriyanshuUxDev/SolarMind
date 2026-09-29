$ErrorActionPreference = 'Stop'

# This launcher is for local development only. Production still uses MySQL,
# environment-backed assumptions, and a separately managed JWT secret.
$repoRoot = Split-Path -Parent $PSScriptRoot
$jwtBytes = New-Object byte[] 32
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($jwtBytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:AI_INTERNAL_TOKEN = 'local-' + [Guid]::NewGuid().ToString('N')
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
