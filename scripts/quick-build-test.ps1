# EyeDock - Quick Build Test (Versão Simplificada)

Write-Host "EyeDock - Teste Rapido de Build" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Gray

# Configurar ambiente
Write-Host "1. Configurando ambiente..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.16.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
$env:ANDROID_HOME = "C:\Users\carlo\AppData\Local\Android\Sdk"
$env:PATH = "$env:ANDROID_HOME\platform-tools;" + $env:PATH

Write-Host "   JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
Write-Host "   ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Gray

# Verificar Java
Write-Host "2. Verificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "   $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   Erro: Java nao encontrado!" -ForegroundColor Red
    exit 1
}

# Clean primeiro
Write-Host "3. Executando clean..." -ForegroundColor Yellow
try {
    $cleanResult = .\gradlew.bat clean 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   Clean OK!" -ForegroundColor Green
    } else {
        Write-Host "   Clean com warnings" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erro no clean: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Build apenas o app simplificado
Write-Host "4. Building app simplificado..." -ForegroundColor Yellow
try {
    Write-Host "   Iniciando build... (pode demorar alguns minutos)" -ForegroundColor Cyan
    $buildResult = .\gradlew.bat :app:assembleDebug 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   BUILD SUCESSO!" -ForegroundColor Green
        
        # Verificar se APK foi gerado
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Host "   APK gerado: $apkPath ($($apkSize.ToString('F1')) MB)" -ForegroundColor Green
            $buildSuccess = $true
        } else {
            Write-Host "   APK nao encontrado no caminho esperado" -ForegroundColor Yellow
            $buildSuccess = $false
        }
    } else {
        Write-Host "   BUILD FALHOU!" -ForegroundColor Red
        Write-Host "   Ultimas linhas do erro:" -ForegroundColor Yellow
        $buildResult | Select-Object -Last 10 | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
        $buildSuccess = $false
    }
} catch {
    Write-Host "   Erro durante build: $($_.Exception.Message)" -ForegroundColor Red
    $buildSuccess = $false
}

# Verificar devices
Write-Host "5. Verificando devices..." -ForegroundColor Yellow
try {
    $devices = adb devices 2>&1
    $deviceLines = $devices | Where-Object { $_ -match "device$" }
    
    if ($deviceLines) {
        Write-Host "   Devices encontrados:" -ForegroundColor Green
        $deviceLines | ForEach-Object { Write-Host "   - $_" -ForegroundColor Gray }
        $hasDevice = $true
    } else {
        Write-Host "   Nenhum device/emulador conectado" -ForegroundColor Yellow
        Write-Host "   Para testar: Abra Android Studio > AVD Manager > Start emulator" -ForegroundColor Gray
        $hasDevice = $false
    }
} catch {
    Write-Host "   ADB nao encontrado" -ForegroundColor Red
    $hasDevice = $false
}

# Resultado final
Write-Host ""
Write-Host "RESULTADO FINAL:" -ForegroundColor Cyan
Write-Host "================" -ForegroundColor Gray
Write-Host "Java: OK" -ForegroundColor Green
Write-Host "Build: $(if($buildSuccess) {'SUCESSO'} else {'FALHOU'})" -ForegroundColor $(if($buildSuccess) {'Green'} else {'Red'})
Write-Host "Device: $(if($hasDevice) {'CONECTADO'} else {'DESCONECTADO'})" -ForegroundColor $(if($hasDevice) {'Green'} else {'Yellow'})

if ($buildSuccess) {
    Write-Host ""
    Write-Host "PROXIMOS PASSOS:" -ForegroundColor Cyan
    if ($hasDevice) {
        Write-Host "1. Instalar app: .\gradlew.bat installDebug" -ForegroundColor White
        Write-Host "2. Iniciar app: adb shell am start -n com.eyedock.app.debug/com.eyedock.app.MainActivity" -ForegroundColor White
        Write-Host "3. Ver logs: adb logcat | grep EyeDock" -ForegroundColor White
    } else {
        Write-Host "1. Abrir Android Studio" -ForegroundColor White
        Write-Host "2. Tools > AVD Manager > Create/Start emulator" -ForegroundColor White
        Write-Host "3. Executar novamente este script" -ForegroundColor White
    }
} else {
    Write-Host ""
    Write-Host "PROBLEMA NO BUILD:" -ForegroundColor Red
    Write-Host "Execute com mais detalhes: .\gradlew.bat :app:assembleDebug --info" -ForegroundColor Yellow
}
