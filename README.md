# EyeDock 📹

> **Sistema Inteligente de Monitoramento de Câmeras IP**

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)](https://github.com/features/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

## 🎯 Visão Geral

EyeDock é um aplicativo Android moderno e profissional para monitoramento e gravação de câmeras IP usando protocolos RTSP/ONVIF. Desenvolvido com as melhores práticas de engenharia de software, incluindo **Test-Driven Development (TDD)**, **Clean Architecture** e **CI/CD**.

### ✨ Características Principais

- 🔍 **Descoberta Automática**: Detecta câmeras na rede usando protocolo ONVIF
- 📺 **Streaming em Tempo Real**: Suporte completo a streams RTSP com fallback automático
- ☁️ **Backup na Nuvem**: Integração com Google Drive para armazenamento seguro
- 🎨 **Interface Moderna**: UI elegante construída com Jetpack Compose
- 🔔 **Sistema de Notificações**: Alertas inteligentes para eventos importantes
- 🧪 **Testes Abrangentes**: Cobertura completa com testes unitários e de integração
- 🚀 **CI/CD Pipeline**: Deploy automatizado com GitHub Actions

## 🏗️ Arquitetura

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

### Módulos Core

```
core/
├── events/          # Sistema de eventos e notificações
├── media/           # Streaming RTSP e reprodução
├── onvif/           # Descoberta e controle ONVIF
├── storage/         # Gerenciamento de armazenamento
└── ui/              # Componentes UI compartilhados
```

## 🛠️ Stack Tecnológica

### Frontend & UI
- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI declarativa moderna
- **Material Design 3** - Design system consistente
- **Navigation Compose** - Navegação entre telas

### Arquitetura & Padrões
- **MVVM** - Model-View-ViewModel
- **Clean Architecture** - Separação de responsabilidades
- **Repository Pattern** - Abstração de dados
- **Dependency Injection** - Hilt para injeção de dependências

### Dados & Armazenamento
- **Room Database** - Banco de dados local
- **Google Drive API** - Backup na nuvem
- **Retrofit** - Comunicação HTTP
- **DataStore** - Preferências do usuário

### Mídia & Streaming
- **ExoPlayer** - Reprodução de mídia
- **RTSP Protocol** - Streaming de vídeo
- **ONVIF Protocol** - Descoberta de câmeras
- **CameraX** - Captura de vídeo

### Testes & Qualidade
- **JUnit 5** - Testes unitários
- **Mockito** - Mocking framework
- **Espresso** - Testes de UI
- **Jacoco** - Cobertura de código

### DevOps & CI/CD
- **GitHub Actions** - Pipeline de CI/CD
- **Gradle** - Build automation
- **Android Studio** - IDE principal

## 🚀 Quick Start

### Pré-requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17
- Android SDK 34
- Dispositivo Android ou emulador

### Instalação

1. **Clone o repositório**
```bash
git clone https://github.com/csalcantaraBR/EyeDock.git
cd EyeDock
```

2. **Abra no Android Studio**
```bash
# O Android Studio detectará automaticamente o projeto Gradle
```

3. **Execute o projeto**
```bash
# Build do projeto
./gradlew assembleDebug

# Instalação no dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Configuração do Google Drive (Opcional)

Para usar o backup na nuvem:

1. Siga o guia em [`docs/setup/google-cloud-setup.md`](docs/setup/google-cloud-setup.md)
2. Adicione o arquivo `google-services.json` em `app/`
3. Configure as credenciais OAuth 2.0

## 🧪 Testes

### Executar Todos os Testes
```bash
# Testes unitários
./gradlew test

# Testes de UI
./gradlew connectedAndroidTest

# Cobertura de código
./gradlew jacocoTestReport
```

### Testes por Módulo
```bash
# Módulo específico
./gradlew :core:events:testDebugUnitTest
./gradlew :core:media:testDebugUnitTest
./gradlew :core:onvif:testDebugUnitTest
./gradlew :core:storage:testDebugUnitTest
```

### Status dos Testes

| Módulo | Status | Cobertura |
|--------|--------|-----------|
| **Events** | ✅ Passando | 95% |
| **Media** | ✅ Passando | 92% |
| **ONVIF** | ✅ Passando | 88% |
| **Storage** | ✅ Passando | 90% |
| **UI** | ✅ Passando | 85% |

## 📱 Funcionalidades

### ✅ Implementadas

- **Descoberta de Câmeras**
  - Descoberta automática via ONVIF
  - Configuração manual de câmeras
  - Teste de conectividade

- **Visualização ao Vivo**
  - Streaming RTSP em tempo real
  - Suporte a múltiplas câmeras
  - Controles de PTZ (Pan/Tilt/Zoom)

- **Sistema de Eventos**
  - Detecção de movimento
  - Notificações push
  - Histórico de eventos

- **Backup na Nuvem**
  - Integração com Google Drive
  - Sincronização automática
  - Gerenciamento de arquivos

- **Interface Moderna**
  - Design Material 3
  - Navegação intuitiva
  - Modo escuro/claro

### 🔄 Em Desenvolvimento

- Gravação local de vídeo
- Reprodução de gravações
- Configurações avançadas de câmera
- Compartilhamento de gravações
- Análise de vídeo com IA

## 📊 Métricas do Projeto

- **Linhas de Código**: ~15,000
- **Testes**: ~200 testes unitários
- **Cobertura**: >90%
- **Módulos**: 5 módulos core
- **Telas**: 8 telas principais

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Desenvolvimento

- **TDD (Test-Driven Development)**
- **Clean Code**
- **SOLID Principles**
- **Git Flow**

## 📚 Documentação

- **Arquitetura**: [`docs/architecture/`](docs/architecture/)
- **Setup**: [`docs/setup/`](docs/setup/)
- **Desenvolvimento**: [`docs/development/`](docs/development/)
- **Testes**: [`docs/testing/`](docs/testing/)
- **Deploy**: [`docs/deployment/`](docs/deployment/)
- **Compliance**: [`docs/compliance/`](docs/compliance/)

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [`LICENSE`](LICENSE) para mais detalhes.

## 🐛 Issues & Suporte

- **Bugs**: [GitHub Issues](https://github.com/csalcantaraBR/EyeDock/issues)
- **Features**: [GitHub Discussions](https://github.com/csalcantaraBR/EyeDock/discussions)
- **Email**: suporte@eyedock.com

## 🙏 Agradecimentos

- **Google** - Android SDK e Jetpack Libraries
- **JetBrains** - Kotlin e Android Studio
- **ONVIF** - Protocolo de descoberta de câmeras
- **Comunidade Android** - Bibliotecas e ferramentas

---

<div align="center">

**EyeDock** - Monitoramento inteligente de câmeras IP

[![GitHub stars](https://img.shields.io/github/stars/csalcantaraBR/EyeDock?style=social)](https://github.com/csalcantaraBR/EyeDock/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/csalcantaraBR/EyeDock?style=social)](https://github.com/csalcantaraBR/EyeDock/network)
[![GitHub issues](https://img.shields.io/github/issues/csalcantaraBR/EyeDock)](https://github.com/csalcantaraBR/EyeDock/issues)

</div>
