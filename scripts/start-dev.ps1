$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Stop-PortProcess([int]$port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        try {
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            Start-Sleep -Milliseconds 500
        } catch {
        }
    }
}

function Resolve-MavenCommand {
    $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvn) {
        return $mvn.Source
    }

    $fallback = Join-Path $env:USERPROFILE '.maven\maven-3.9.12\bin\mvn.cmd'
    if (Test-Path $fallback) {
        return $fallback
    }

    throw 'No se encontró Maven. Instala Maven o agrega mvn al PATH.'
}

function Resolve-NpmCommand {
    $npm = Get-Command npm -ErrorAction SilentlyContinue
    if ($npm) {
        return $npm.Source
    }

    throw 'No se encontró npm. Instala Node.js o agrega npm al PATH.'
}

Write-Host 'Deteniendo procesos previos en puertos 8080 y 4200...' -ForegroundColor Yellow
Stop-PortProcess 8080
Stop-PortProcess 4200

$mvnCmd = Resolve-MavenCommand
$npmCmd = Resolve-NpmCommand

$oldBootstrapUser = $env:APP_BOOTSTRAP_ADMIN_USERNAME
$oldBootstrapEmail = $env:APP_BOOTSTRAP_ADMIN_EMAIL
$oldBootstrapPass = $env:APP_BOOTSTRAP_ADMIN_PASSWORD
$oldJwtSecret = $env:JWT_SECRET

if ([string]::IsNullOrWhiteSpace($env:APP_BOOTSTRAP_ADMIN_USERNAME)) {
    $env:APP_BOOTSTRAP_ADMIN_USERNAME = 'admin'
}
if ([string]::IsNullOrWhiteSpace($env:APP_BOOTSTRAP_ADMIN_EMAIL)) {
    $env:APP_BOOTSTRAP_ADMIN_EMAIL = 'admin@sgilib.dev'
}
if ([string]::IsNullOrWhiteSpace($env:APP_BOOTSTRAP_ADMIN_PASSWORD)) {
    $env:APP_BOOTSTRAP_ADMIN_PASSWORD = 'change-me-admin-password'
}
if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    $env:JWT_SECRET = 'change-me-jwt-secret-min-32-characters'
}

Write-Host 'Iniciando backend (Spring Boot, profile=test)...' -ForegroundColor Cyan
$backend = Start-Process -FilePath $mvnCmd `
    -WorkingDirectory (Join-Path $root 'backend') `
    -ArgumentList @('spring-boot:run', '-Dspring-boot.run.profiles=test', '-Dspring-boot.run.useTestClasspath=true') `
    -WindowStyle Minimized -PassThru

Write-Host 'Iniciando frontend (Angular)...' -ForegroundColor Cyan
$frontend = Start-Process -FilePath $npmCmd `
    -WorkingDirectory (Join-Path $root 'frontend') `
    -ArgumentList @('start') `
    -WindowStyle Minimized -PassThru

Write-Host 'Iniciando desktop (Java Swing) con sync API...' -ForegroundColor Cyan
$oldSync = $env:SGILIB_DESKTOP_API_SYNC
$oldApiBase = $env:SGILIB_API_BASE
$oldApiUser = $env:SGILIB_API_USER
$oldApiPass = $env:SGILIB_API_PASSWORD

$env:SGILIB_DESKTOP_API_SYNC = 'true'
$env:SGILIB_API_BASE = 'http://localhost:8080/api/v1'
$env:SGILIB_API_USER = $env:APP_BOOTSTRAP_ADMIN_USERNAME
$env:SGILIB_API_PASSWORD = $env:APP_BOOTSTRAP_ADMIN_PASSWORD

$desktop = Start-Process -FilePath 'java.exe' `
    -WorkingDirectory $root `
    -ArgumentList @('LibreriaApp') `
    -PassThru

$env:SGILIB_DESKTOP_API_SYNC = $oldSync
$env:SGILIB_API_BASE = $oldApiBase
$env:SGILIB_API_USER = $oldApiUser
$env:SGILIB_API_PASSWORD = $oldApiPass
$env:APP_BOOTSTRAP_ADMIN_USERNAME = $oldBootstrapUser
$env:APP_BOOTSTRAP_ADMIN_EMAIL = $oldBootstrapEmail
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = $oldBootstrapPass
$env:JWT_SECRET = $oldJwtSecret

Write-Host ''
Write-Host 'Servicios lanzados:' -ForegroundColor Green
Write-Host ('  backend  PID=' + $backend.Id)
Write-Host ('  frontend PID=' + $frontend.Id)
Write-Host ('  desktop  PID=' + $desktop.Id)
Write-Host ''
Write-Host 'Health backend: http://localhost:8080/actuator/health' -ForegroundColor DarkGray
Write-Host 'Frontend:      http://localhost:4200' -ForegroundColor DarkGray
Write-Host ''
Write-Host 'Para detener todo: .\scripts\stop-dev.ps1' -ForegroundColor Yellow
