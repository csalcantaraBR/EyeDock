# 🔧 Scripts EyeDock

> **Scripts de automação para desenvolvimento e teste**

## 📋 Índice dos Scripts

### 🚀 Execução
Scripts para executar e testar o aplicativo.

- [`run-app.ps1`](run-app.ps1) - Executa o aplicativo no dispositivo/emulador
- [`quick-test.ps1`](quick-test.ps1) - Executa testes rápidos
- [`quick-build-test.ps1`](quick-build-test.ps1) - Build e teste completo

### 🛠️ Setup & Configuração
Scripts para configurar o ambiente de desenvolvimento.

- [`install-android-studio.ps1`](install-android-studio.ps1) - Instala o Android Studio
- [`setup-avd-emulator.ps1`](setup-avd-emulator.ps1) - Configura emulador AVD
- [`setup-local-testing.ps1`](setup-local-testing.ps1) - Setup para testes locais

### 🧪 Testes
Scripts específicos para testes.

- [`test-app-locally.ps1`](test-app-locally.ps1) - Testa o aplicativo localmente

## 🚀 Como Usar

### Pré-requisitos
- PowerShell 5.1 ou superior
- Permissões de execução de scripts habilitadas

### Habilitar Execução de Scripts
```powershell
# Execute como administrador
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Executar Scripts
```powershell
# Navegue para a pasta scripts
cd scripts

# Execute um script
.\run-app.ps1
```

## 📝 Descrição dos Scripts

### `run-app.ps1`
Executa o aplicativo EyeDock no dispositivo ou emulador conectado.

**Uso:**
```powershell
.\run-app.ps1
```

**Funcionalidades:**
- Build do projeto
- Instalação no dispositivo
- Execução do aplicativo

### `quick-test.ps1`
Executa testes unitários rápidos do projeto.

**Uso:**
```powershell
.\quick-test.ps1
```

**Funcionalidades:**
- Execução de testes unitários
- Relatório de resultados
- Validação de build

### `quick-build-test.ps1`
Executa build completo e todos os testes.

**Uso:**
```powershell
.\quick-build-test.ps1
```

**Funcionalidades:**
- Build completo do projeto
- Testes unitários
- Testes de integração
- Relatório de cobertura

### `install-android-studio.ps1`
Instala o Android Studio automaticamente.

**Uso:**
```powershell
.\install-android-studio.ps1
```

**Funcionalidades:**
- Download do Android Studio
- Instalação automática
- Configuração inicial

### `setup-avd-emulator.ps1`
Configura um emulador Android Virtual Device (AVD).

**Uso:**
```powershell
.\setup-avd-emulator.ps1
```

**Funcionalidades:**
- Criação de AVD
- Configuração de hardware
- Instalação de sistema Android

### `setup-local-testing.ps1`
Configura o ambiente para testes locais.

**Uso:**
```powershell
.\setup-local-testing.ps1
```

**Funcionalidades:**
- Configuração de variáveis de ambiente
- Setup de dependências
- Validação do ambiente

### `test-app-locally.ps1`
Executa testes completos do aplicativo localmente.

**Uso:**
```powershell
.\test-app-locally.ps1
```

**Funcionalidades:**
- Testes unitários
- Testes de UI
- Testes de integração
- Relatórios detalhados

## 🔧 Personalização

### Variáveis de Ambiente
Os scripts usam as seguintes variáveis de ambiente:

```powershell
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:GRADLE_HOME = "C:\gradle-8.5"
```

### Configuração Personalizada
Para personalizar os scripts:

1. Edite as variáveis no início de cada script
2. Ajuste os caminhos conforme seu ambiente
3. Modifique os parâmetros conforme necessário

## 🐛 Troubleshooting

### Problemas Comuns

**Erro de execução de scripts:**
```powershell
# Solução: Habilitar execução de scripts
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

**Erro de permissão:**
```powershell
# Solução: Executar como administrador
Start-Process PowerShell -Verb RunAs
```

**Erro de caminho:**
```powershell
# Solução: Verificar variáveis de ambiente
echo $env:ANDROID_HOME
echo $env:JAVA_HOME
```

## 📞 Suporte

Para problemas com os scripts:

1. Verifique os logs de erro
2. Confirme as variáveis de ambiente
3. Teste em um ambiente limpo
4. Abra uma issue no GitHub

---

**Última atualização**: Dezembro 2024  
**Versão**: 1.0.0  
**Mantido por**: Equipe EyeDock
