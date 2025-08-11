# EyeDock - Setup Script para Testes Locais

Write-Host "EyeDock - Setup para Testes Locais" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Gray

# 1. Verificar Java
Write-Host "1. Verificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "   Java encontrado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   Java nao encontrado. Configure JAVA_HOME." -ForegroundColor Red
    $env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.16.8-hotspot"
    $env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
}

# 2. Configurar Android SDK
Write-Host "2. Configurando Android SDK..." -ForegroundColor Yellow
$androidSdkPath = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

if (Test-Path $androidSdkPath) {
    Write-Host "   Android SDK encontrado: $androidSdkPath" -ForegroundColor Green
    $env:ANDROID_HOME = $androidSdkPath
    $env:ANDROID_SDK_ROOT = $androidSdkPath
} else {
    Write-Host "   Android SDK nao encontrado. Instale Android Studio." -ForegroundColor Yellow
}

# 3. Atualizar local.properties
Write-Host "3. Configurando local.properties..." -ForegroundColor Yellow
$content = "sdk.dir=" + ($androidSdkPath -replace '\\', '\\\\')
$content | Out-File -FilePath "local.properties" -Encoding ASCII
Write-Host "   local.properties atualizado" -ForegroundColor Green

# 4. Testar Gradle
Write-Host "4. Testando Gradle..." -ForegroundColor Yellow
try {
    $result = .\gradlew.bat clean 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   Gradle funcionando!" -ForegroundColor Green
    } else {
        Write-Host "   Gradle com warnings" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Erro no Gradle" -ForegroundColor Red
}

# Mostrar status
Write-Host ""
Write-Host "STATUS:" -ForegroundColor Cyan
Write-Host "Java: $(if($javaVersion) {'OK'} else {'MISSING'})" -ForegroundColor White
Write-Host "Android SDK: $(if(Test-Path $androidSdkPath) {'OK'} else {'MISSING'})" -ForegroundColor White
Write-Host ""

Write-Host "PROXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "1. Instalar Android Studio (se necessario)" -ForegroundColor White
Write-Host "2. Criar AVD (emulador Android)" -ForegroundColor White
Write-Host "3. Executar: .\gradlew.bat assembleDebug" -ForegroundColor White
Write-Host "4. Executar: .\gradlew.bat installDebug" -ForegroundColor White
Write-Host ""