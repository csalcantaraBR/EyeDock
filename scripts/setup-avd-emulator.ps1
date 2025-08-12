# EyeDock - AVD Emulator Setup Script

Write-Host "EyeDock - Configuracao do Emulador Android" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Gray

# Verificar caminhos do Android Studio
$androidSdkPath = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$avdManager = "$androidSdkPath\cmdline-tools\latest\bin\avdmanager.bat"
$sdkManager = "$androidSdkPath\cmdline-tools\latest\bin\sdkmanager.bat"
$emulator = "$androidSdkPath\emulator\emulator.exe"

Write-Host "1. Verificando instalacao do Android SDK..." -ForegroundColor Yellow

if (Test-Path $androidSdkPath) {
    Write-Host "   Android SDK encontrado: $androidSdkPath" -ForegroundColor Green
    $env:ANDROID_HOME = $androidSdkPath
    $env:ANDROID_SDK_ROOT = $androidSdkPath
    $env:PATH = "$androidSdkPath\platform-tools;$androidSdkPath\emulator;$androidSdkPath\cmdline-tools\latest\bin;" + $env:PATH
} else {
    Write-Host "   Android SDK nao encontrado!" -ForegroundColor Red
    Write-Host "   Execute primeiro o Android Studio para configurar o SDK" -ForegroundColor Yellow
    exit 1
}

# Verificar se cmdline-tools existe
if (-not (Test-Path $avdManager)) {
    Write-Host "   Command-line tools nao encontrados" -ForegroundColor Yellow
    Write-Host "   Instale via Android Studio: SDK Manager > Android SDK Command-line Tools" -ForegroundColor Yellow
    
    # Tentar caminho alternativo
    $altCmdTools = "$androidSdkPath\tools\bin\avdmanager.bat"
    if (Test-Path $altCmdTools) {
        $avdManager = $altCmdTools
        $sdkManager = "$androidSdkPath\tools\bin\sdkmanager.bat"
        Write-Host "   Usando tools alternativos encontrados" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "CONFIGURACAO MANUAL NECESSARIA:" -ForegroundColor Red
        Write-Host "1. Abrir Android Studio" -ForegroundColor White
        Write-Host "2. File > Settings > Android SDK" -ForegroundColor White
        Write-Host "3. SDK Tools tab" -ForegroundColor White
        Write-Host "4. Marcar 'Android SDK Command-line Tools'" -ForegroundColor White
        Write-Host "5. Apply/OK para instalar" -ForegroundColor White
        Write-Host "6. Executar este script novamente" -ForegroundColor White
        exit 1
    }
}

Write-Host "2. Verificando emuladores existentes..." -ForegroundColor Yellow

# Listar AVDs existentes
try {
    $existingAvds = & $avdManager list avd 2>$null | Where-Object { $_ -match "Name:" }
    if ($existingAvds) {
        Write-Host "   AVDs encontrados:" -ForegroundColor Green
        $existingAvds | ForEach-Object { Write-Host "   - $($_ -replace 'Name: ', '')" -ForegroundColor Gray }
        
        # Verificar se ja temos um EyeDock AVD
        $eyeDockAvd = $existingAvds | Where-Object { $_ -match "EyeDock" }
        if ($eyeDockAvd) {
            Write-Host "   AVD EyeDock_Test ja existe!" -ForegroundColor Green
            $avdExists = $true
        } else {
            $avdExists = $false
        }
    } else {
        Write-Host "   Nenhum AVD encontrado" -ForegroundColor Yellow
        $avdExists = $false
    }
} catch {
    Write-Host "   Erro ao verificar AVDs: $($_.Exception.Message)" -ForegroundColor Red
    $avdExists = $false
}

# Criar AVD se nao existir
if (-not $avdExists) {
    Write-Host ""
    Write-Host "3. Criando AVD para EyeDock..." -ForegroundColor Yellow
    
    # Verificar se system images estao instaladas
    Write-Host "   Verificando system images..." -ForegroundColor Cyan
    
    $systemImage = "system-images;android-34;google_apis;x86_64"
    
    try {
        Write-Host "   Instalando system image: $systemImage" -ForegroundColor Cyan
        & $sdkManager $systemImage --channel=0
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   System image instalada!" -ForegroundColor Green
        } else {
            Write-Host "   Aviso: System image pode nao ter sido instalada corretamente" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   Erro ao instalar system image: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Criar AVD
    Write-Host "   Criando AVD EyeDock_Test..." -ForegroundColor Cyan
    
    try {
        $avdName = "EyeDock_Test"
        $deviceType = "pixel_6"
        
        # Comando para criar AVD
        $createAvdCmd = "echo no | `"$avdManager`" create avd -n $avdName -k `"$systemImage`" -d $deviceType"
        
        Invoke-Expression $createAvdCmd
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   AVD criado com sucesso!" -ForegroundColor Green
            $avdExists = $true
        } else {
            Write-Host "   Erro ao criar AVD" -ForegroundColor Red
        }
    } catch {
        Write-Host "   Erro durante criacao do AVD: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Configurar AVD
if ($avdExists) {
    Write-Host ""
    Write-Host "4. Configurando AVD..." -ForegroundColor Yellow
    
    $avdConfigPath = "$env:USERPROFILE\.android\avd\EyeDock_Test.avd\config.ini"
    
    if (Test-Path $avdConfigPath) {
        Write-Host "   Otimizando configuracoes do AVD..." -ForegroundColor Cyan
        
        # Configurações otimizadas para desenvolvimento
        $config = @"
hw.ramSize=4096
vm.heapSize=256
hw.gpu.enabled=yes
hw.gpu.mode=auto
hw.keyboard=yes
hw.camera.back=webcam0
hw.camera.front=webcam0
showDeviceFrame=no
"@
        
        Add-Content -Path $avdConfigPath -Value $config
        Write-Host "   AVD configurado para desenvolvimento!" -ForegroundColor Green
    }
}

# Instruções finais
Write-Host ""
Write-Host "5. Como usar o emulador..." -ForegroundColor Yellow

Write-Host ""
Write-Host "COMANDOS UTEIS:" -ForegroundColor Cyan
Write-Host "# Listar AVDs disponiveis" -ForegroundColor White
Write-Host "`"$avdManager`" list avd" -ForegroundColor Gray
Write-Host ""
Write-Host "# Iniciar emulador" -ForegroundColor White
Write-Host "`"$emulator`" -avd EyeDock_Test" -ForegroundColor Gray
Write-Host ""
Write-Host "# Verificar devices conectados" -ForegroundColor White
Write-Host "adb devices" -ForegroundColor Gray
Write-Host ""

# Status final
Write-Host "STATUS FINAL:" -ForegroundColor Cyan
Write-Host "Android SDK: $(if(Test-Path $androidSdkPath) {'CONFIGURADO'} else {'PENDENTE'})" -ForegroundColor White
Write-Host "Command-line Tools: $(if(Test-Path $avdManager) {'INSTALADO'} else {'PENDENTE'})" -ForegroundColor White
Write-Host "AVD EyeDock_Test: $(if($avdExists) {'CRIADO'} else {'PENDENTE'})" -ForegroundColor White

if ($avdExists) {
    Write-Host ""
    Write-Host "PRONTO! Execute o proximo script:" -ForegroundColor Green
    Write-Host ".\test-app-locally.ps1" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "OU inicie o emulador manualmente:" -ForegroundColor Green
    Write-Host "`"$emulator`" -avd EyeDock_Test" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "CONFIGURACAO MANUAL NECESSARIA:" -ForegroundColor Yellow
    Write-Host "1. Abrir Android Studio" -ForegroundColor White
    Write-Host "2. Tools > AVD Manager" -ForegroundColor White
    Write-Host "3. Create Virtual Device" -ForegroundColor White
    Write-Host "4. Escolher Pixel 6 ou similar" -ForegroundColor White
    Write-Host "5. API Level 34 (recomendado)" -ForegroundColor White
    Write-Host "6. Finish para criar" -ForegroundColor White
}
