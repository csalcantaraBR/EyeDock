# EyeDock - Local Testing Script

Write-Host "EyeDock - Teste Local da Aplicacao" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Gray

# Configurar variaveis de ambiente
$androidSdkPath = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$env:ANDROID_HOME = $androidSdkPath
$env:ANDROID_SDK_ROOT = $androidSdkPath
$env:PATH = "$androidSdkPath\platform-tools;$androidSdkPath\emulator;" + $env:PATH

Write-Host "1. Verificando ambiente..." -ForegroundColor Yellow

# Verificar ADB
try {
    $adbVersion = adb version 2>&1 | Select-Object -First 1
    Write-Host "   ADB: $adbVersion" -ForegroundColor Green
} catch {
    Write-Host "   ADB nao encontrado!" -ForegroundColor Red
    exit 1
}

# Verificar devices conectados
Write-Host "   Verificando devices conectados..." -ForegroundColor Cyan
$devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device" }

if ($devices) {
    Write-Host "   Devices encontrados:" -ForegroundColor Green
    $devices | ForEach-Object { Write-Host "   - $($_.Split()[0])" -ForegroundColor Gray }
    $deviceConnected = $true
} else {
    Write-Host "   Nenhum device/emulador conectado" -ForegroundColor Yellow
    Write-Host "   Inicie o emulador antes de continuar" -ForegroundColor Yellow
    $deviceConnected = $false
}

Write-Host ""
Write-Host "2. Preparando build..." -ForegroundColor Yellow

# Clean build
Write-Host "   Executando clean..." -ForegroundColor Cyan
try {
    $cleanResult = .\gradlew.bat clean 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   Clean executado com sucesso!" -ForegroundColor Green
    } else {
        Write-Host "   Clean executado com warnings" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erro no clean: $($_.Exception.Message)" -ForegroundColor Red
}

# Build debug APK
Write-Host "   Building debug APK..." -ForegroundColor Cyan
try {
    $buildResult = .\gradlew.bat assembleDebug 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   APK construido com sucesso!" -ForegroundColor Green
        $buildSuccess = $true
        
        # Mostrar tamanho do APK
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Host "   APK size: $($apkSize.ToString('F1')) MB" -ForegroundColor Gray
        }
    } else {
        Write-Host "   Erro no build!" -ForegroundColor Red
        $buildSuccess = $false
    }
} catch {
    Write-Host "   Erro durante build: $($_.Exception.Message)" -ForegroundColor Red
    $buildSuccess = $false
}

# Instalar se build foi bem-sucedido e device conectado
if ($buildSuccess -and $deviceConnected) {
    Write-Host ""
    Write-Host "3. Instalando aplicacao..." -ForegroundColor Yellow
    
    try {
        Write-Host "   Desinstalando versao anterior (se existir)..." -ForegroundColor Cyan
        adb uninstall com.eyedock.app.debug 2>$null
        
        Write-Host "   Instalando nova versao..." -ForegroundColor Cyan
        $installResult = .\gradlew.bat installDebug 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   App instalado com sucesso!" -ForegroundColor Green
            $installSuccess = $true
        } else {
            Write-Host "   Erro na instalacao!" -ForegroundColor Red
            $installSuccess = $false
        }
    } catch {
        Write-Host "   Erro durante instalacao: $($_.Exception.Message)" -ForegroundColor Red
        $installSuccess = $false
    }
} else {
    $installSuccess = $false
}

# Executar testes se tudo estiver ok
if ($installSuccess) {
    Write-Host ""
    Write-Host "4. Executando testes automatizados..." -ForegroundColor Yellow
    
    try {
        Write-Host "   Executando unit tests..." -ForegroundColor Cyan
        $testResult = .\gradlew.bat testDebugUnitTest 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   Unit tests passaram!" -ForegroundColor Green
        } else {
            Write-Host "   Unit tests com problemas" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   Erro nos testes: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Iniciar monitoramento de logs
if ($installSuccess) {
    Write-Host ""
    Write-Host "5. Iniciando monitoramento..." -ForegroundColor Yellow
    
    Write-Host "   Limpando log buffer..." -ForegroundColor Cyan
    adb logcat -c
    
    Write-Host "   Iniciando app EyeDock..." -ForegroundColor Cyan
    adb shell am start -n com.eyedock.app.debug/com.eyedock.app.MainActivity
    
    Start-Sleep -Seconds 2
    
    Write-Host "   Capturando logs iniciais..." -ForegroundColor Cyan
    $logs = adb logcat -d | Where-Object { $_ -match "EyeDock" } | Select-Object -Last 10
    
    if ($logs) {
        Write-Host "   Logs encontrados:" -ForegroundColor Green
        $logs | ForEach-Object { Write-Host "   $($_)" -ForegroundColor Gray }
    } else {
        Write-Host "   Nenhum log EyeDock encontrado ainda" -ForegroundColor Yellow
    }
}

# Checklist de validacao manual
Write-Host ""
Write-Host "6. CHECKLIST DE VALIDACAO MANUAL:" -ForegroundColor Cyan
Write-Host ""

$checklist = @(
    "App inicia sem crash",
    "Splash screen aparece",
    "MainActivity carrega",
    "Bottom navigation funciona",
    "FAB 'Add Camera' aparece", 
    "Permissoes de camera solicitadas",
    "QR Scanner abre sem erro",
    "Storage picker funciona",
    "Navegacao entre telas OK",
    "Themes aplicam corretamente",
    "Performance adequada",
    "Memory usage razoavel"
)

$checklist | ForEach-Object { Write-Host "[ ] $_" -ForegroundColor White }

# Comandos uteis para debugging
Write-Host ""
Write-Host "7. COMANDOS DE DEBUG:" -ForegroundColor Cyan
Write-Host ""
Write-Host "# Monitorar logs em tempo real:" -ForegroundColor White
Write-Host "adb logcat | grep EyeDock" -ForegroundColor Gray
Write-Host ""
Write-Host "# Verificar memory usage:" -ForegroundColor White
Write-Host "adb shell dumpsys meminfo com.eyedock.app.debug" -ForegroundColor Gray
Write-Host ""
Write-Host "# Clear app data:" -ForegroundColor White  
Write-Host "adb shell pm clear com.eyedock.app.debug" -ForegroundColor Gray
Write-Host ""
Write-Host "# Reinstalar app:" -ForegroundColor White
Write-Host ".\gradlew.bat installDebug" -ForegroundColor Gray
Write-Host ""
Write-Host "# Restart app:" -ForegroundColor White
Write-Host "adb shell am start -n com.eyedock.app.debug/com.eyedock.app.MainActivity" -ForegroundColor Gray

# Status final
Write-Host ""
Write-Host "STATUS FINAL:" -ForegroundColor Cyan
Write-Host "Build: $(if($buildSuccess) {'SUCCESS'} else {'FAILED'})" -ForegroundColor White
Write-Host "Install: $(if($installSuccess) {'SUCCESS'} else {'FAILED'})" -ForegroundColor White
Write-Host "Device: $(if($deviceConnected) {'CONNECTED'} else {'DISCONNECTED'})" -ForegroundColor White

if ($installSuccess) {
    Write-Host ""
    Write-Host "SUCESSO! EyeDock instalado e rodando!" -ForegroundColor Green
    Write-Host "Valide manualmente usando o checklist acima." -ForegroundColor Green
    Write-Host ""
    Write-Host "Para monitorar em tempo real:" -ForegroundColor Cyan
    Write-Host "adb logcat | grep EyeDock" -ForegroundColor Yellow
} else {
    Write-Host ""
    if (-not $deviceConnected) {
        Write-Host "PROBLEMA: Emulador nao conectado" -ForegroundColor Red
        Write-Host "Solucao: Inicie o emulador primeiro:" -ForegroundColor Yellow
        Write-Host "`"$androidSdkPath\emulator\emulator.exe`" -avd EyeDock_Test" -ForegroundColor Cyan
    } elseif (-not $buildSuccess) {
        Write-Host "PROBLEMA: Build falhou" -ForegroundColor Red
        Write-Host "Solucao: Verifique erros de compilacao acima" -ForegroundColor Yellow
    } else {
        Write-Host "PROBLEMA: Instalacao falhou" -ForegroundColor Red
        Write-Host "Solucao: Verifique permissoes e espaco no device" -ForegroundColor Yellow
    }
}
