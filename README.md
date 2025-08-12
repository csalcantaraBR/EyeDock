# EyeDock 📹

> **Intelligent IP Camera Monitoring System**

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)](https://github.com/features/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

## 🎯 Overview

EyeDock is a modern and professional Android application for monitoring and recording IP cameras using RTSP/ONVIF protocols. Developed with software engineering best practices, including **Test-Driven Development (TDD)**, **Clean Architecture**, and **CI/CD**.

### ✨ Key Features

- 🔍 **Automatic Discovery**: Detects cameras on the network using ONVIF protocol
- 📺 **Real-time Streaming**: Complete RTSP stream support with automatic fallback
- ☁️ **Cloud Backup**: Google Drive integration for secure storage
- 🎨 **Modern Interface**: Elegant UI built with Jetpack Compose
- 🔔 **Notification System**: Intelligent alerts for important events
- 🧪 **Comprehensive Testing**: Full coverage with unit and integration tests
- 🚀 **CI/CD Pipeline**: Automated deployment with GitHub Actions

## 🏗️ Architecture

### Clean Architecture + MVVM

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Screens   │  │ ViewModels  │  │ Navigation  │        │
│  │  (Compose)  │  │   (MVVM)    │  │             │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Use Cases  │  │  Entities   │  │ Interfaces  │        │
│  │             │  │             │  │             │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │Repository   │  │   Local     │  │   Remote    │        │
│  │             │  │  (Room DB)  │  │  (APIs)     │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### Core Modules

```
core/
├── events/          # Event system and notifications
├── media/           # RTSP streaming and playback
├── onvif/           # ONVIF discovery and control
├── storage/         # Storage management
└── ui/              # Shared UI components
```

## 🛠️ Technology Stack

### Frontend & UI
- **Kotlin** - Primary language
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Consistent design system
- **Navigation Compose** - Screen navigation

### Architecture & Patterns
- **MVVM** - Model-View-ViewModel
- **Clean Architecture** - Separation of concerns
- **Repository Pattern** - Data abstraction
- **Dependency Injection** - Hilt for dependency injection

### Data & Storage
- **Room Database** - Local database
- **Google Drive API** - Cloud backup
- **Retrofit** - HTTP communication
- **DataStore** - User preferences

### Media & Streaming
- **ExoPlayer** - Media playback
- **RTSP Protocol** - Video streaming
- **ONVIF Protocol** - Camera discovery
- **CameraX** - Video capture

### Testing & Quality
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Espresso** - UI testing
- **Jacoco** - Code coverage

### DevOps & CI/CD
- **GitHub Actions** - CI/CD pipeline
- **Gradle** - Build automation
- **Android Studio** - Primary IDE

## 🚀 Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or higher
- JDK 17
- Android SDK 34
- Android device or emulator

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/csalcantaraBR/EyeDock.git
cd EyeDock
```

2. **Open in Android Studio**
```bash
# Android Studio will automatically detect the Gradle project
```

3. **Run the project**
```bash
# Build the project
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Google Drive Configuration (Optional)

To use cloud backup:

1. Follow the guide in [`docs/setup/google-cloud-setup.md`](docs/setup/google-cloud-setup.md)
2. Add the `google-services.json` file to `app/`
3. Configure OAuth 2.0 credentials

## 🧪 Testing

### Run All Tests
```bash
# Unit tests
./gradlew test

# UI tests
./gradlew connectedAndroidTest

# Code coverage
./gradlew jacocoTestReport
```

### Tests by Module
```bash
# Specific module
./gradlew :core:events:testDebugUnitTest
./gradlew :core:media:testDebugUnitTest
./gradlew :core:onvif:testDebugUnitTest
./gradlew :core:storage:testDebugUnitTest
```

### Test Status

| Module | Status | Coverage |
|--------|--------|----------|
| **Events** | ✅ Passing | 95% |
| **Media** | ✅ Passing | 92% |
| **ONVIF** | ✅ Passing | 88% |
| **Storage** | ✅ Passing | 90% |
| **UI** | ✅ Passing | 85% |

## 📱 Features

### ✅ Implemented

- **Camera Discovery**
  - Automatic discovery via ONVIF
  - Manual camera configuration
  - Connectivity testing

- **Live Viewing**
  - Real-time RTSP streaming
  - Multiple camera support
  - PTZ controls (Pan/Tilt/Zoom)

- **Event System**
  - Motion detection
  - Push notifications
  - Event history

- **Cloud Backup**
  - Google Drive integration
  - Automatic synchronization
  - File management

- **Modern Interface**
  - Material Design 3
  - Intuitive navigation
  - Dark/light mode

### 🔄 In Development

- Local video recording
- Recording playback
- Advanced camera settings
- Recording sharing
- AI video analysis

## 📊 Project Metrics

- **Lines of Code**: ~15,000
- **Tests**: ~200 unit tests
- **Coverage**: >90%
- **Modules**: 5 core modules
- **Screens**: 8 main screens

## 🤝 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Standards

- **TDD (Test-Driven Development)**
- **Clean Code**
- **SOLID Principles**
- **Git Flow**

## 📚 Documentation

- **Architecture**: [`docs/architecture/`](docs/architecture/)
- **Setup**: [`docs/setup/`](docs/setup/)
- **Development**: [`docs/development/`](docs/development/)
- **Testing**: [`docs/testing/`](docs/testing/)
- **Deploy**: [`docs/deployment/`](docs/deployment/)
- **Compliance**: [`docs/compliance/`](docs/compliance/)

## 📄 License

This project is licensed under the MIT License. See the [`LICENSE`](LICENSE) file for details.

## 🐛 Issues & Support

- **Bugs**: [GitHub Issues](https://github.com/csalcantaraBR/EyeDock/issues)
- **Features**: [GitHub Discussions](https://github.com/csalcantaraBR/EyeDock/discussions)
- **Email**: support@eyedock.com

## 🙏 Acknowledgments

- **Google** - Android SDK and Jetpack Libraries
- **JetBrains** - Kotlin and Android Studio
- **ONVIF** - Camera discovery protocol
- **Android Community** - Libraries and tools

---

<div align="center">

**EyeDock** - Intelligent IP camera monitoring

[![GitHub stars](https://img.shields.io/github/stars/csalcantaraBR/EyeDock?style=social)](https://github.com/csalcantaraBR/EyeDock/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/csalcantaraBR/EyeDock?style=social)](https://github.com/csalcantaraBR/EyeDock/network)
[![GitHub issues](https://img.shields.io/github/issues/csalcantaraBR/EyeDock)](https://github.com/csalcantaraBR/EyeDock/issues)

</div>
