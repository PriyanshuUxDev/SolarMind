$ErrorActionPreference = 'Stop'

$serviceRoot = Split-Path -Parent $PSScriptRoot

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

Import-DotEnv (Join-Path $serviceRoot '..\.env')
Import-DotEnv (Join-Path $serviceRoot '.env')

$required = @('AI_INTERNAL_TOKEN', 'GEMINI_API_KEY', 'GEMINI_MODEL')
$missing = $required | Where-Object {
    [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_))
}
if ($missing.Count -gt 0) {
    throw "AI service configuration is missing: $($missing -join ', '). Set these variables in ai-service\.env or the repository .env."
}

$python = Get-Command python.exe -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py.exe -ErrorAction SilentlyContinue }
$uv = Get-Command uv.exe -ErrorAction SilentlyContinue
if (-not $python -and -not $uv) {
    throw 'Python was not found. Install Python 3.11+ or uv before starting the AI service.'
}

Push-Location $serviceRoot
try {
    if ($uv) {
        & $uv.Source run --with-requirements requirements.txt python -m uvicorn main:app --reload --port 8000
    } elseif ($python.Name -ieq 'py.exe') {
        & $python.Source -3 -m uvicorn main:app --reload --port 8000
    } else {
        & $python.Source -m uvicorn main:app --reload --port 8000
    }
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
finally {
    Pop-Location
}
