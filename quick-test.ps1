# EyeDock - Quick Test

Write-Host "=== EyeDock Quick Test ===" -ForegroundColor Cyan

# 1. Configurar Android SDK
Write-Host "1. Configurando Android SDK..." -ForegroundColor Yellow
$env:ANDROID_HOME = "C:\Users\carlo\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
Write-Host "   ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Gray

# 2. Verificar se SDK existe
if (Test-Path $env:ANDROID_HOME) {
    Write-Host "   SDK encontrado!" -ForegroundColor Green
} else {
    Write-Host "   SDK NAO encontrado!" -ForegroundColor Red
    Write-Host "   Verifique se o Android Studio configurou o SDK em:" -ForegroundColor Yellow
    Write-Host "   $env:ANDROID_HOME" -ForegroundColor Gray
    exit 1
}

# 3. Verificar Gradle
Write-Host "2. Testando Gradle..." -ForegroundColor Yellow
try {
    Write-Host "   Executando gradlew --version..." -ForegroundColor Gray
    $gradleVersion = .\gradlew.bat --version 2>&1 | Select-Object -First 3
    $gradleVersion | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
    Write-Host "   Gradle OK!" -ForegroundColor Green
} catch {
    Write-Host "   Erro no Gradle: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Testar clean
Write-Host "3. Testando clean..." -ForegroundColor Yellow
try {
    $cleanOutput = .\gradlew.bat clean 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   Clean OK!" -ForegroundColor Green
    } else {
        Write-Host "   Clean com problemas (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
        Write-Host "   Output: $($cleanOutput | Select-Object -Last 5)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   Erro no clean: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Verificar ADB
Write-Host "4. Verificando ADB..." -ForegroundColor Yellow
$env:PATH = "$env:ANDROID_HOME\platform-tools;" + $env:PATH
try {
    $adbVersion = adb version 2>&1 | Select-Object -First 1
    Write-Host "   $adbVersion" -ForegroundColor Green
    
    Write-Host "   Devices conectados:" -ForegroundColor Gray
    $devices = adb devices 2>&1
    $devices | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} catch {
    Write-Host "   ADB nao encontrado" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Resultado ===" -ForegroundColor Cyan
Write-Host "Se tudo estiver OK acima, execute:" -ForegroundColor White
Write-Host ".\gradlew.bat assembleDebug" -ForegroundColor Yellow
Write-Host ""
Write-Host "Para criar emulador:" -ForegroundColor White
Write-Host "1. Abrir Android Studio" -ForegroundColor Gray
Write-Host "2. Tools > AVD Manager" -ForegroundColor Gray
Write-Host "3. Create Virtual Device" -ForegroundColor Gray
