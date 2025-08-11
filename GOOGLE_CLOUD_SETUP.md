# Configuração do Google Cloud Console para EyeDock

## Problema Atual
O app está recebendo erro 10 (DEVELOPER_ERROR) porque a configuração OAuth não está correta.

## Solução: Configurar Google Cloud Console

### 1. Criar Projeto no Google Cloud Console

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Clique em "Selecionar projeto" > "Novo projeto"
3. Nome: `EyeDock App`
4. Clique em "Criar"

### 2. Ativar APIs Necessárias

1. No menu lateral, vá para "APIs e serviços" > "Biblioteca"
2. Procure e ative:
   - **Google Drive API**
   - **Google Sign-In API**

### 3. Configurar OAuth 2.0

1. Vá para "APIs e serviços" > "Credenciais"
2. Clique em "Criar credenciais" > "ID do cliente OAuth 2.0"
3. Selecione "Aplicativo Android"
4. Configure:
   - **Nome**: `EyeDock Android App`
   - **Nome do pacote**: `com.eyedock.app.debug`
   - **SHA-1**: `52:61:3E:3E:E7:F6:65:01:50:7A:57:3F:C3:55:14:1F:38:9E:7B:45`

### 4. SHA-1 do Certificado (Já Obtido)

O SHA-1 do certificado debug já foi obtido:
```
SHA1: 52:61:3E:3E:E7:F6:65:01:50:7A:57:3F:C3:55:14:1F:38:9E:7B:45
```

### 5. Baixar google-services.json

1. No Google Cloud Console, vá para "Configurações do projeto"
2. Clique em "Baixar google-services.json"
3. Substitua o arquivo em `app/google-services.json`

### 6. Configurar Permissões OAuth

1. Vá para "APIs e serviços" > "Tela de consentimento OAuth"
2. Configure:
   - **Tipo de usuário**: Externo
   - **Nome do app**: EyeDock
   - **Email de suporte**: seu-email@gmail.com
   - **Domínio autorizado**: (deixe vazio para desenvolvimento)

### 7. Adicionar Escopos

1. Na tela de consentimento, vá para "Escopos"
2. Adicione:
   - `https://www.googleapis.com/auth/drive.file`
   - `https://www.googleapis.com/auth/userinfo.email`

### 8. Adicionar Usuários de Teste

1. Na tela de consentimento, vá para "Usuários de teste"
2. Clique em "Adicionar usuários"
3. Adicione seu email Gmail que será usado para testar

## Teste da Configuração

Após configurar:

1. Compile o app: `./gradlew assembleDebug`
2. Instale: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Teste o sign-in
4. Verifique os logs: `adb logcat -s GoogleDriveStorage`

## Logs Esperados (Sucesso)

```
GoogleDriveStorage: Sign-in successful: seu-email@gmail.com
GoogleDriveStorage: Drive service initialized for account: seu-email@gmail.com
GoogleDriveStorage: EyeDock folder created successfully: EyeDock Recordings (ID: 1abc...)
```

## Problemas Comuns

### Erro 10: DEVELOPER_ERROR
- Verifique se o SHA-1 está correto: `52:61:3E:3E:E7:F6:65:01:50:7A:57:3F:C3:55:14:1F:38:9E:7B:45`
- Confirme se o package_name está correto: `com.eyedock.app.debug`
- Verifique se as APIs estão ativadas
- Confirme se você está na lista de usuários de teste

### Erro 7: NETWORK_ERROR
- Verifique conexão com internet
- Confirme se o Google Play Services está atualizado

### Erro 12501: SIGN_IN_CANCELLED
- Usuário cancelou o sign-in (normal)

## Para Produção

1. Configure usuários de teste na tela de consentimento
2. Use certificado de release real
3. Configure domínios autorizados
4. Publique o app no Google Play Store

## Resumo das Configurações Necessárias

- **Package Name**: `com.eyedock.app.debug`
- **SHA-1**: `52:61:3E:3E:E7:F6:65:01:50:7A:57:3F:C3:55:14:1F:38:9E:7B:45`
- **APIs**: Google Drive API, Google Sign-In API
- **Escopos**: `https://www.googleapis.com/auth/drive.file`, `https://www.googleapis.com/auth/userinfo.email`
