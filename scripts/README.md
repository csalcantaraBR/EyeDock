# 🔧 EyeDock Scripts

> **Automation scripts for development and testing**

## 📋 Scripts Index

### 🚀 Execution
Scripts to run and test the application.

- [`run-app.ps1`](run-app.ps1) - Runs the application on device/emulator
- [`quick-test.ps1`](quick-test.ps1) - Runs quick tests
- [`quick-build-test.ps1`](quick-build-test.ps1) - Complete build and test

### 🛠️ Setup & Configuration
Scripts to configure the development environment.

- [`install-android-studio.ps1`](install-android-studio.ps1) - Installs Android Studio
- [`setup-avd-emulator.ps1`](setup-avd-emulator.ps1) - Configures AVD emulator
- [`setup-local-testing.ps1`](setup-local-testing.ps1) - Setup for local testing

### 🧪 Testing
Specific testing scripts.

- [`test-app-locally.ps1`](test-app-locally.ps1) - Tests the application locally

## 🚀 How to Use

### Prerequisites
- PowerShell 5.1 or higher
- Script execution permissions enabled

### Enable Script Execution
```powershell
# Run as administrator
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Run Scripts
```powershell
# Navigate to scripts folder
cd scripts

# Run a script
.\run-app.ps1
```

## 📝 Script Descriptions

### `run-app.ps1`
Runs the EyeDock application on connected device or emulator.

**Usage:**
```powershell
.\run-app.ps1
```

**Features:**
- Project build
- Device installation
- Application execution

### `quick-test.ps1`
Runs quick unit tests for the project.

**Usage:**
```powershell
.\quick-test.ps1
```

**Features:**
- Unit test execution
- Results reporting
- Build validation

### `quick-build-test.ps1`
Runs complete build and all tests.

**Usage:**
```powershell
.\quick-build-test.ps1
```

**Features:**
- Complete project build
- Unit tests
- Integration tests
- Coverage report

### `install-android-studio.ps1`
Automatically installs Android Studio.

**Usage:**
```powershell
.\install-android-studio.ps1
```

**Features:**
- Android Studio download
- Automatic installation
- Initial configuration

### `setup-avd-emulator.ps1`
Configures an Android Virtual Device (AVD) emulator.

**Usage:**
```powershell
.\setup-avd-emulator.ps1
```

**Features:**
- AVD creation
- Hardware configuration
- Android system installation

### `setup-local-testing.ps1`
Configures the environment for local testing.

**Usage:**
```powershell
.\setup-local-testing.ps1
```

**Features:**
- Environment variable configuration
- Dependency setup
- Environment validation

### `test-app-locally.ps1`
Runs complete application tests locally.

**Usage:**
```powershell
.\test-app-locally.ps1
```

**Features:**
- Unit tests
- UI tests
- Integration tests
- Detailed reports

## 🔧 Customization

### Environment Variables
The scripts use the following environment variables:

```powershell
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:GRADLE_HOME = "C:\gradle-8.5"
```

### Custom Configuration
To customize the scripts:

1. Edit variables at the beginning of each script
2. Adjust paths according to your environment
3. Modify parameters as needed

## 🐛 Troubleshooting

### Common Issues

**Script execution error:**
```powershell
# Solution: Enable script execution
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

**Permission error:**
```powershell
# Solution: Run as administrator
Start-Process PowerShell -Verb RunAs
```

**Path error:**
```powershell
# Solution: Check environment variables
echo $env:ANDROID_HOME
echo $env:JAVA_HOME
```

## 📞 Support

For script issues:

1. Check error logs
2. Confirm environment variables
3. Test in a clean environment
4. Open an issue on GitHub

---

**Last updated**: December 2024  
**Version**: 1.0.0  
**Maintained by**: EyeDock Team
