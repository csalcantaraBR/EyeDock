# EyeDock - Android IP Camera Manager

[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com/about/versions/14)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern Android application for managing IP cameras with ONVIF support, QR code scanning, and real-time video streaming.

## 🎯 Features

### 📱 Core Functionality
- **QR Code Scanning**: Add cameras by scanning QR codes
- **ONVIF Discovery**: Automatic camera discovery on local network
- **RTSP Streaming**: Real-time video streaming with ExoPlayer
- **PTZ Controls**: Pan, Tilt, Zoom controls for compatible cameras
- **Secure Storage**: Encrypted credential storage
- **Modern UI**: Material 3 design with Jetpack Compose

### 🔧 Technical Features
- **Multi-module Architecture**: Clean separation of concerns
- **Dependency Injection**: Hilt for dependency management
- **Coroutines & Flow**: Asynchronous programming
- **CameraX Integration**: QR code scanning with ML Kit
- **Network Security**: Secure HTTP client with OkHttp
- **Local Storage**: In-memory device repository (Room ready)

## 🏗️ Architecture

```
EyeDock/
├── app/                    # Main application module
│   ├── data/              # Data layer implementations
│   ├── domain/            # Domain models and interfaces
│   ├── ui/                # UI components and screens
│   ├── viewmodels/        # ViewModels
│   └── di/                # Dependency injection
├── core/                  # Core modules (planned)
│   ├── onvif/            # ONVIF protocol implementation
│   ├── media/            # Media handling
│   ├── storage/          # Data persistence
│   ├── common/           # Shared utilities
│   ├── events/           # Event handling
│   └── ui/               # Shared UI components
└── buildSrc/             # Build configuration
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK API 34
- Kotlin 1.9.0+
- Java 17+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/eyedock.git
   cd eyedock
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build and Run**
   ```bash
   # Using Gradle wrapper
   ./gradlew assembleDebug
   
   # Install on connected device/emulator
   ./gradlew installDebug
   ```

### Development Setup

1. **Sync Project**
   - Android Studio will automatically sync the project
   - If not, click "Sync Project with Gradle Files"

2. **Run on Emulator**
   - Create an Android Virtual Device (AVD)
   - Run the app from Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## 📋 Usage

### Adding Cameras

#### QR Code Scanning
1. Open the app and navigate to "Cameras" tab
2. Tap "Scan QR" button
3. Grant camera permission when prompted
4. Point camera at QR code containing camera details
5. Camera will be automatically added

#### Manual Addition
1. Tap "Add Camera" button
2. Fill in camera details:
   - **Name**: Friendly name for the camera
   - **IP Address**: Camera's IP address
   - **RTSP URL**: Stream URL (e.g., `rtsp://192.168.1.100:554/stream1`)
   - **Username/Password**: Authentication credentials (optional)
3. Tap "Add Camera"

#### Network Discovery
1. Tap "Discover cameras" button
2. App will scan local network for ONVIF cameras
3. Select cameras to add from the discovered list

### Viewing Cameras
- **Live View**: Tap any camera to view live stream
- **PTZ Controls**: Use on-screen controls for pan/tilt/zoom
- **Snapshot**: Take screenshots of current view
- **Settings**: Configure camera-specific settings

## 🛠️ Development

### Project Structure

#### Domain Layer
- **Models**: Data classes for camera connections, ONVIF endpoints
- **Interfaces**: Contracts for QR parsing, ONVIF client, RTSP prober
- **Use Cases**: Business logic for onboarding and PTZ operations

#### Data Layer
- **Repositories**: Device management and credential storage
- **Implementations**: ONVIF client, RTSP prober, ExoPlayer integration
- **Security**: Encrypted shared preferences for credentials

#### UI Layer
- **Screens**: Main app screens (Cameras, Live View, etc.)
- **Components**: Reusable UI components
- **Navigation**: App navigation using Compose Navigation

### Key Technologies

- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Latest Material Design components
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **Flow**: Reactive streams
- **CameraX**: Camera functionality
- **ML Kit**: QR code scanning
- **ExoPlayer**: Media playback
- **OkHttp**: Network requests
- **ONVIF**: IP camera protocol

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Lint check
./gradlew lint
```

## 📱 Screenshots

*Screenshots will be added here*

## 🔮 Roadmap

### Phase 1 (Current) ✅
- [x] QR code scanning
- [x] ONVIF discovery
- [x] Basic RTSP streaming
- [x] PTZ controls
- [x] Secure credential storage

### Phase 2 (Next)
- [ ] Advanced ONVIF features
- [ ] Multiple stream support
- [ ] Recording functionality
- [ ] Motion detection
- [ ] Push notifications

### Phase 3 (Future)
- [ ] Cloud storage integration
- [ ] Multi-camera layouts
- [ ] Advanced analytics
- [ ] AI-powered features
- [ ] Web dashboard

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Add tests for new features
- Update documentation as needed

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [ONVIF](https://www.onvif.org/) for IP camera standards
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI
- [ExoPlayer](https://exoplayer.dev/) for media playback
- [Material Design](https://material.io/) for design system

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/eyedock/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/eyedock/discussions)
- **Email**: support@eyedock.app

---

**EyeDock** - Professional IP Camera Management for Android
