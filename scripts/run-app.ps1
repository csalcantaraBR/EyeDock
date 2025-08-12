# Script para executar o EyeDock no emulador
Write-Host "=== EyeDock - Executando no Emulador ===" -ForegroundColor Green

# 1. Verificar se o emulador está rodando
Write-Host "1. Verificando emulador..." -ForegroundColor Yellow
$devices = adb devices
Write-Host "Dispositivos conectados: $devices"

# 2. Compilar o projeto
Write-Host "2. Compilando o projeto..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug

# 3. Instalar o APK
Write-Host "3. Instalando o APK..." -ForegroundColor Yellow
adb install app\build\outputs\apk\debug\app-debug.apk

# 4. Executar o app
Write-Host "4. Executando o app..." -ForegroundColor Yellow
adb shell am start -n com.eyedock.app.debug/com.eyedock.app.MainActivity

Write-Host "=== App executado com sucesso! ===" -ForegroundColor Green
Write-Host "Verifique o emulador para ver o EyeDock rodando!" -ForegroundColor Cyan
