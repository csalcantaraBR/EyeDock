# EyeDock - Android IP Camera Manager

A modern Android application for managing IP cameras with ONVIF support, QR code scanning, and real-time video streaming.

## Key Features
- 📱 QR Code Scanning for easy camera setup
- 🔍 ONVIF Discovery for automatic camera detection
- 📺 RTSP Streaming with ExoPlayer
- 🎮 PTZ Controls (Pan, Tilt, Zoom)
- 🔒 Secure credential storage
- 🎨 Material 3 UI with Jetpack Compose

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt
- **Async**: Coroutines & Flow
- **Media**: ExoPlayer for RTSP streaming
- **Camera**: CameraX + ML Kit for QR scanning
- **Network**: OkHttp for ONVIF/RTSP communication

## Quick Start
```bash
git clone https://github.com/yourusername/eyedock.git
cd eyedock
./gradlew assembleDebug
```

Perfect for security professionals, home automation enthusiasts, and anyone needing professional IP camera management on Android.

## License
MIT License - see LICENSE file for details.
