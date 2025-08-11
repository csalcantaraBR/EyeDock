# EyeDock - Sistema de Monitoramento de Câmeras

EyeDock é um aplicativo Android moderno para monitoramento e gravação de câmeras IP usando protocolos RTSP/ONVIF.

## 🚀 Funcionalidades

### ✅ Implementadas
- **Descoberta de Câmeras**: Descoberta automática de câmeras na rede usando ONVIF
- **Conexão RTSP**: Suporte a streams RTSP com fallback automático
- **Interface Moderna**: UI construída com Jetpack Compose
- **Navegação**: Sistema de navegação entre telas
- **Teste de Conexão**: Teste de conectividade RTSP
- **Backup na Nuvem**: Integração com Google Drive (configuração necessária)
- **Sistema de Eventos**: Gerenciamento de eventos de câmeras
- **Notificações**: Sistema de notificações push

### 🔄 Em Desenvolvimento
- Gravação de vídeo
- Reprodução de gravações
- Configurações avançadas de câmera
- Detecção de movimento
- Compartilhamento de gravações

## 🛠️ Tecnologias

- **Kotlin**: Linguagem principal
- **Jetpack Compose**: UI moderna
- **MVVM**: Arquitetura de apresentação
- **Room**: Banco de dados local
- **Retrofit**: Comunicação de rede
- **ExoPlayer**: Reprodução de mídia
- **Google Drive API**: Backup na nuvem
- **ONVIF**: Descoberta de câmeras
- **RTSP**: Streaming de vídeo

## 📱 Telas Principais

1. **Tela Principal**: Lista de câmeras conectadas
2. **Adicionar Câmera**: Configuração manual ou descoberta automática
3. **Visualização ao Vivo**: Stream em tempo real
4. **Backup na Nuvem**: Gerenciamento de arquivos no Google Drive
5. **Configurações**: Configurações do aplicativo

## 🏗️ Arquitetura

```
app/
├── data/
│   ├── cloud/          # Integração Google Drive
│   ├── local/          # Banco de dados Room
│   ├── network/        # Comunicação de rede
│   └── repository/     # Repositórios
├── domain/
│   ├── interfaces/     # Interfaces de domínio
│   └── model/          # Modelos de dados
├── presentation/
│   ├── screens/        # Telas Compose
│   ├── viewmodels/     # ViewModels
│   └── navigation/     # Navegação
└── utils/              # Utilitários

core/
├── events/             # Sistema de eventos
├── media/              # Streaming RTSP
├── onvif/              # Descoberta ONVIF
├── storage/            # Armazenamento local
└── ui/                 # Componentes UI compartilhados
```

## 🚀 Como Executar

### Pré-requisitos
- Android Studio Hedgehog ou superior
- JDK 17
- Android SDK 34
- Dispositivo Android ou emulador

### Configuração
1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/eyedock.git
cd eyedock
```

2. Abra o projeto no Android Studio

3. Configure o Google Drive (opcional):
   - Siga o guia em `GOOGLE_CLOUD_SETUP.md`
   - Adicione o arquivo `google-services.json` em `app/`

4. Execute o projeto:
```bash
./gradlew assembleDebug
```

### Instalação no Dispositivo
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 🧪 Testes

### Executar Testes Unitários
```bash
# Todos os testes
./gradlew test

# Módulo específico
./gradlew :core:events:testDebugUnitTest
./gradlew :core:media:testDebugUnitTest
```

### Executar Testes de UI
```bash
./gradlew connectedAndroidTest
```

## 📊 Status dos Testes

- ✅ **Events Module**: Testes passando
- 🔄 **Media Module**: Em correção
- ⏳ **ONVIF Module**: Pendente
- ⏳ **Storage Module**: Pendente
- ⏳ **UI Module**: Pendente

## 🔧 Configuração do Google Drive

Para usar o backup na nuvem:

1. Crie um projeto no [Google Cloud Console](https://console.cloud.google.com/)
2. Ative as APIs:
   - Google Drive API
   - Google Sign-In API
3. Configure OAuth 2.0 com:
   - Package name: `com.eyedock.app.debug`
   - SHA-1: `52:61:3E:3E:E7:F6:65:01:50:7A:57:3F:C3:55:14:1F:38:9E:7B:45`
4. Baixe o `google-services.json` e coloque em `app/`
5. Adicione usuários de teste na tela de consentimento OAuth

Veja o guia completo em `GOOGLE_CLOUD_SETUP.md`.

## 📝 Logs

Para monitorar os logs do aplicativo:
```bash
adb logcat -s EyeDock CloudBackupViewModel GoogleDriveStorage CloudBackupRepository
```

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 🐛 Problemas Conhecidos

- Google Sign-In requer configuração OAuth adequada
- Alguns testes ainda estão em correção
- Funcionalidade de gravação em desenvolvimento

## 📞 Suporte

Para suporte, abra uma issue no GitHub ou entre em contato através do email: suporte@eyedock.com

---

**EyeDock** - Monitoramento inteligente de câmeras IP
