# EyeDock - IP Camera Management App

A modern Android application for managing IP cameras with real-time streaming, network discovery, and comprehensive camera management features.

## 🚀 Features

### ✅ Implemented Features

#### 📱 Core Functionality
- **Camera Management**: Add, view, and manage multiple IP cameras
- **Real-time Streaming**: Live video streaming using ExoPlayer with RTSP support
- **Network Discovery**: Automatic camera discovery on local network with validation
- **Manual Setup**: Manual camera configuration with IP, port, and credentials
- **QR Code Scanning**: Camera setup via QR code scanning (framework ready)

#### 🔍 Network Discovery
- **Smart Scanning**: Intelligent network scanning with host reachability checks
- **RTSP Validation**: Rigorous camera validation using RTSP OPTIONS requests
- **Timeout Management**: Configurable timeouts (45 seconds) with progress indicators
- **Multiple Paths**: Automatic RTSP path retry mechanism for compatibility

#### 🎥 Live View
- **ExoPlayer Integration**: High-performance video streaming
- **Error Handling**: Comprehensive error handling with retry mechanisms
- **Player Controls**: Play/pause, mute/unmute functionality
- **Connection Status**: Real-time connection status indicators

#### 🏗️ Architecture
- **MVVM Pattern**: Clean architecture with ViewModels and StateFlow
- **Dependency Injection**: Manual DI with AppModule
- **Repository Pattern**: Data layer abstraction
- **Room Database**: Local camera storage with SQLite

### 🔧 Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Repository Pattern
- **Video Player**: ExoPlayer (Media3)
- **Database**: Room (SQLite)
- **Networking**: Kotlin Coroutines + Socket connections
- **Build System**: Gradle (Kotlin DSL)

## 📱 Screenshots

### Add Camera Screen
- Three options: QR Scan, Manual Entry, Network Discovery
- Optimized layout with proper text visibility
- Material 3 design with consistent spacing

### Network Discovery
- Real-time network scanning with progress indicators
- Validated camera detection with RTSP testing
- Clean UI with device cards and status information

### Live View
- Full-screen video streaming
- Player controls and status indicators
- Error handling with retry options

## 🛠️ Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34+
- Kotlin 1.9+
- JDK 17+

### Installation
1. Clone the repository:
```bash
git clone https://github.com/yourusername/EyeDock.git
cd EyeDock
```

2. Open in Android Studio and sync Gradle

3. Build and run on device or emulator:
```bash
./gradlew assembleDebug
```

### Network Requirements
- Device must be on the same network as cameras
- Cameras must support RTSP protocol
- Network discovery requires local network access

## 🔧 Configuration

### Camera Setup
1. **Network Discovery**: Automatically finds cameras on network
2. **Manual Entry**: Enter IP, port, username, and password
3. **QR Code**: Scan camera QR code (if available)

### RTSP Paths Supported
- `/onvif1`
- `/live/ch00_0`
- `/live/ch0`
- `/live`
- `/cam/realmonitor`
- `/video1`
- `/video`
- `/stream1`
- `/stream`

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- ViewModels: Core business logic testing
- Repository: Data layer testing
- Network: Connectivity and validation testing

## 📊 Project Structure

```
app/
├── src/main/kotlin/com/eyedock/app/
│   ├── screens/           # Compose UI screens
│   ├── viewmodels/        # MVVM ViewModels
│   ├── data/             # Data layer
│   │   ├── local/        # Room database
│   │   ├── repository/   # Repository implementations
│   │   └── player/       # ExoPlayer implementation
│   ├── domain/           # Domain models and interfaces
│   ├── network/          # Network discovery
│   ├── utils/            # Utility classes
│   └── di/               # Dependency injection
└── src/test/             # Unit tests
```

## 🚀 Recent Updates

### v1.0.0 - Core Implementation
- ✅ Live video streaming with ExoPlayer
- ✅ Network discovery with RTSP validation
- ✅ Manual camera setup
- ✅ Optimized UI layout and text visibility
- ✅ Comprehensive error handling
- ✅ MVVM architecture implementation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Known Issues

- Network discovery may take up to 45 seconds on large networks
- Some camera models may require specific RTSP paths
- Emulator testing has network limitations (use physical device)

## 🔮 Roadmap

- [ ] PTZ camera controls
- [ ] Video recording functionality
- [ ] Snapshot capture
- [ ] Fullscreen mode
- [ ] Camera grouping
- [ ] Push notifications
- [ ] Cloud storage integration

---

**EyeDock** - Making IP camera management simple and efficient.
