$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Stop-PortProcess([int]$port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        try {
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            Write-Host ("Puerto " + $port + " liberado (PID " + $conn.OwningProcess + ")") -ForegroundColor Yellow
        } catch {
        }
    }
}

Stop-PortProcess 8080
Stop-PortProcess 4200

$desktopJava = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -match 'LibreriaApp' }

foreach ($p in $desktopJava) {
    try {
        Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
        Write-Host ("Desktop detenido (PID " + $p.ProcessId + ")") -ForegroundColor Yellow
    } catch {
    }
}

$shellChildren = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
    Where-Object {
        ($_.Name -eq 'cmd.exe' -or $_.Name -eq 'powershell.exe') -and (
            $_.CommandLine -match 'spring-boot:run' -or
            $_.CommandLine -match 'npm start' -or
            $_.CommandLine -match 'LibreriaApp'
        )
    }

foreach ($p in $shellChildren) {
    try {
        Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
    } catch {
    }
}

Write-Host 'Procesos principales detenidos.' -ForegroundColor Green
