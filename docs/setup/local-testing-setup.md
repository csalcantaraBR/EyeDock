# 📱 EyeDock - Setup para Testes Locais

## 🎯 Objetivo
Configurar ambiente completo para testar a aplicação EyeDock localmente e verificar todas as funcionalidades.

## 📋 Pré-requisitos Necessários

### 1. **Android Studio**
- [ ] Android Studio Hedgehog | 2023.1.1 ou superior
- [ ] Android SDK Platform 34 (API 34)
- [ ] Android SDK Build-Tools 34.0.0
- [ ] Android Emulator ou dispositivo físico

### 2. **SDKs e Ferramentas**
- [ ] Android SDK Platform-Tools
- [ ] Android SDK Command-line Tools
- [ ] Google Play Services
- [ ] Google Repository

### 3. **Dispositivo de Teste**
- [ ] Emulador Android (API 26+ recomendado API 33/34)
- [ ] OU dispositivo físico com Android 8.0+ (API 26+)
- [ ] Habilitado "Depuração USB" se dispositivo físico

## 🛠️ Configuração Passo a Passo

### Passo 1: Configurar Android SDK
```bash
# Verificar se ANDROID_HOME está configurado
echo $ANDROID_HOME  # Linux/Mac
echo %ANDROID_HOME% # Windows

# Se não estiver configurado, adicionar às variáveis de ambiente:
# Windows: C:\Users\[USER]\AppData\Local\Android\Sdk
# Mac: ~/Library/Android/sdk
# Linux: ~/Android/Sdk
```

### Passo 2: Configurar Gradle Properties
```bash
# Criar/editar ~/.gradle/gradle.properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.configureondemand=true
android.useAndroidX=true
android.enableJetifier=true
```

### Passo 3: Configurar Variáveis de Ambiente
```bash
# Adicionar ao PATH (Windows)
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$env:PATH = "$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\tools;$env:PATH"

# Verificar ADB
adb version
```

## 🔧 Configuração do Projeto EyeDock

### 1. **Atualizar build.gradle.kts para execução real**
```kotlin
// Habilitar recursos necessários para teste local
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.eyedock.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

### 2. **Configurar dependências completas**
```kotlin
dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Camera & ML Kit
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // Media
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

## 📱 Cenários de Teste Local

### 1. **Teste de Inicialização**
- [ ] App inicia sem crashes
- [ ] Splash screen aparece
- [ ] Tela principal (Cameras) carrega
- [ ] Bottom navigation funciona
- [ ] FAB "Add Camera" aparece

### 2. **Teste de Permissões**
- [ ] Solicita permissão de câmera para QR
- [ ] Solicita permissão de microfone para audio
- [ ] Solicita permissão de notificações
- [ ] Storage Access Framework funciona

### 3. **Teste de UI Components**
- [ ] QR Scanner abre sem erro
- [ ] Camera wall mostra estado vazio
- [ ] Formulário manual funciona
- [ ] Navigation entre telas funciona
- [ ] Themes (dark/light) aplicam corretamente

### 4. **Teste de Storage SAF**
- [ ] Storage picker abre
- [ ] Usuário consegue selecionar pasta
- [ ] URI permissions são salvas
- [ ] Teste de escrita de arquivo

### 5. **Teste de Networking (Mock)**
- [ ] Discovery de dispositivos (simulado)
- [ ] Conexão RTSP (simulada)
- [ ] Timeout handling funciona

## 🧪 Comandos de Teste

### Build e Deploy
```bash
# Build debug
./gradlew assembleDebug

# Install no device/emulator
./gradlew installDebug

# Run tests
./gradlew testDebug

# Run instrumentation tests
./gradlew connectedDebugAndroidTest

# Lint check
./gradlew lintDebug
```

### Debugging
```bash
# Ver logs em tempo real
adb logcat | grep EyeDock

# Ver logs específicos
adb logcat | grep -E "(EyeDock|ONVIF|RTSP|SAF)"

# Clear app data
adb shell pm clear com.eyedock.app
```

## 🔍 Checklist de Validação Manual

### ✅ **Funcionalidades Core**
- [ ] **App Launch**: Inicia sem crash
- [ ] **Navigation**: Bottom nav + screens funcionam
- [ ] **Permissions**: Requests aparecem e funcionam
- [ ] **Storage SAF**: Picker funciona e salva URI
- [ ] **QR Scanner**: Camera preview aparece
- [ ] **UI Responsiveness**: Sem lag ou freeze
- [ ] **Memory**: Sem vazamentos evidentes
- [ ] **Themes**: Dark/light mode funciona

### ✅ **Cenários de Erro**
- [ ] **No Camera**: Graceful fallback
- [ ] **No Permissions**: Mensagens claras
- [ ] **No Storage**: SAF error handling
- [ ] **Network Offline**: Appropriate messages
- [ ] **Low Memory**: App não crasha

### ✅ **Performance**
- [ ] **Cold Start**: < 3 segundos
- [ ] **Navigation**: Transições suaves
- [ ] **Scroll**: Listas fluidas
- [ ] **Memory Usage**: < 200MB normal usage

## 📊 Ferramentas de Monitoramento

### Android Studio Profiler
- [ ] CPU usage
- [ ] Memory allocation
- [ ] Network activity
- [ ] Energy consumption

### ADB Commands
```bash
# Memory info
adb shell dumpsys meminfo com.eyedock.app

# CPU info
adb shell top | grep eyedock

# Storage usage
adb shell du -sh /data/data/com.eyedock.app

# Network stats
adb shell cat /proc/net/dev
```

## 🐛 Debug Tips

### Logging Strategy
```kotlin
// Add throughout app for debugging
Log.d("EyeDock", "Feature X: State Y")
Log.i("EyeDock_ONVIF", "Discovery started")
Log.w("EyeDock_Storage", "SAF permission missing")
Log.e("EyeDock_Media", "RTSP connection failed", exception)
```

### Breakpoints Strategy
- MainActivity.onCreate()
- Navigation destinations
- Permission request callbacks
- SAF document picker results
- Network connection attempts

## 🎯 Expected Results

### **Success Criteria**
1. **✅ App runs without crashes**
2. **✅ All major screens accessible**
3. **✅ Permissions work correctly**
4. **✅ Storage SAF functional**
5. **✅ UI responsive and smooth**
6. **✅ Memory usage reasonable**
7. **✅ Error handling graceful**

### **Known Limitations (Expected)**
- ONVIF discovery will be simulated (no real cameras)
- RTSP streaming will show mock data
- Some features require real IP cameras

## 🚀 Next Steps After Local Testing

1. **✅ Validate core functionality**
2. **🔧 Fix any discovered issues**
3. **📱 Test on multiple devices/API levels**
4. **🧪 Add real camera integration**
5. **📦 Prepare release build**
6. **🚀 Deploy to Play Store**

---

**Pronto para iniciar os testes locais! Vamos configurar o ambiente Android?**
