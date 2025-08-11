# EyeDock - Android Studio Installation Script

Write-Host "EyeDock - Instalacao do Android Studio" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Gray

# Verificar se Android Studio ja esta instalado
$androidStudioPath = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$studioExe = Get-ChildItem -Path "C:\Users\$env:USERNAME\AppData\Local\JetBrains\Toolbox\apps\AndroidStudio\ch-*\*\bin\studio64.exe" -ErrorAction SilentlyContinue
$studioExe2 = "C:\Program Files\Android\Android Studio\bin\studio64.exe"

Write-Host "1. Verificando instalacao existente..." -ForegroundColor Yellow

if (Test-Path $studioExe2) {
    Write-Host "   Android Studio encontrado: $studioExe2" -ForegroundColor Green
    $studioInstalled = $true
} elseif ($studioExe) {
    Write-Host "   Android Studio encontrado via Toolbox: $($studioExe.FullName)" -ForegroundColor Green
    $studioInstalled = $true
} else {
    Write-Host "   Android Studio nao encontrado" -ForegroundColor Yellow
    $studioInstalled = $false
}

if (Test-Path $androidStudioPath) {
    Write-Host "   Android SDK encontrado: $androidStudioPath" -ForegroundColor Green
    $sdkInstalled = $true
} else {
    Write-Host "   Android SDK nao encontrado" -ForegroundColor Yellow
    $sdkInstalled = $false
}

# Se nao estiver instalado, tentar instalar via winget
if (-not $studioInstalled) {
    Write-Host ""
    Write-Host "2. Instalando Android Studio..." -ForegroundColor Yellow
    
    try {
        Write-Host "   Tentando instalar via winget..." -ForegroundColor Cyan
        winget install Google.AndroidStudio
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   Android Studio instalado com sucesso!" -ForegroundColor Green
            $studioInstalled = $true
        } else {
            Write-Host "   Falha na instalacao via winget" -ForegroundColor Red
        }
    } catch {
        Write-Host "   Erro durante instalacao: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Configurar SDK se necessario
Write-Host ""
Write-Host "3. Configurando Android SDK..." -ForegroundColor Yellow

if ($sdkInstalled) {
    # Atualizar variaveis de ambiente
    $env:ANDROID_HOME = $androidStudioPath
    $env:ANDROID_SDK_ROOT = $androidStudioPath
    $env:PATH = "$androidStudioPath\platform-tools;$androidStudioPath\tools;$androidStudioPath\cmdline-tools\latest\bin;" + $env:PATH
    
    Write-Host "   ANDROID_HOME configurado: $env:ANDROID_HOME" -ForegroundColor Green
    
    # Atualizar local.properties
    $localProps = "sdk.dir=" + ($androidStudioPath -replace '\\', '\\\\')
    $localProps | Out-File -FilePath "local.properties" -Encoding ASCII
    Write-Host "   local.properties atualizado" -ForegroundColor Green
} else {
    Write-Host "   SDK sera configurado na primeira execucao do Android Studio" -ForegroundColor Yellow
}

# Instruções manuais se necessario
Write-Host ""
Write-Host "4. Proximos passos..." -ForegroundColor Yellow

if ($studioInstalled) {
    Write-Host "   Android Studio instalado!" -ForegroundColor Green
    Write-Host ""
    Write-Host "CONFIGURACAO NECESSARIA:" -ForegroundColor Cyan
    Write-Host "1. Abrir Android Studio" -ForegroundColor White
    Write-Host "2. Aceitar licencas do SDK" -ForegroundColor White
    Write-Host "3. Instalar componentes necessarios:" -ForegroundColor White
    Write-Host "   - Android SDK Platform 34" -ForegroundColor Gray
    Write-Host "   - Android SDK Build-Tools 34.0.0" -ForegroundColor Gray
    Write-Host "   - Android Emulator" -ForegroundColor Gray
    Write-Host "   - Google Play Services" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "   INSTALACAO MANUAL NECESSARIA:" -ForegroundColor Red
    Write-Host ""
    Write-Host "1. Baixar Android Studio:" -ForegroundColor White
    Write-Host "   https://developer.android.com/studio" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "2. Executar instalador baixado" -ForegroundColor White
    Write-Host "3. Seguir wizard de instalacao" -ForegroundColor White
    Write-Host "4. Configurar SDK path para:" -ForegroundColor White
    Write-Host "   $androidStudioPath" -ForegroundColor Gray
    Write-Host ""
}

# Status final
Write-Host "STATUS FINAL:" -ForegroundColor Cyan
Write-Host "Android Studio: $(if($studioInstalled) {'INSTALADO'} else {'PENDENTE'})" -ForegroundColor White
Write-Host "Android SDK: $(if($sdkInstalled) {'CONFIGURADO'} else {'PENDENTE'})" -ForegroundColor White

if ($studioInstalled -and $sdkInstalled) {
    Write-Host ""
    Write-Host "PRONTO! Execute o proximo script:" -ForegroundColor Green
    Write-Host ".\setup-avd-emulator.ps1" -ForegroundColor Cyan
}
