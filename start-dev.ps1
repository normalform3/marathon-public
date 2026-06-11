$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendDir = Join-Path $repoRoot "marathon-web-frontend"

if (-not (Test-Path $frontendDir)) {
    Write-Error "Cannot find frontend directory: $frontendDir"
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Maven (mvn) not found. Please install Maven and add it to PATH."
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "npm not found. Please install Node.js and add it to PATH."
}

$nodeModules = Join-Path $frontendDir "node_modules"
if (-not (Test-Path $nodeModules)) {
    Write-Host "Installing frontend dependencies..."
    Push-Location $frontendDir
    try {
        npm install
    }
    finally {
        Pop-Location
    }
}

$backendCmd = "Set-Location `"$repoRoot`"; mvn -pl marathon-webapp spring-boot:run"
$frontendCmd = "Set-Location `"$frontendDir`"; npm run dev"

Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd | Out-Null
Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd | Out-Null

Write-Host "Started backend and frontend in separate PowerShell windows."
