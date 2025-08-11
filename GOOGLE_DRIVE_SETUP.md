# Configuração do Google Drive para EyeDock

## Configuração Atual

O EyeDock está configurado para usar o Google Drive real para backup das gravações. A configuração atual inclui:

### 1. Dependências
- Google Play Services Auth
- Google Drive API
- Google API Client

### 2. Arquivo de Configuração
- `app/google-services.json` - Configuração básica para desenvolvimento

## Para Produção

Para usar o Google Drive real em produção, você precisa:

### 1. Criar um Projeto no Google Cloud Console
1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto ou use um existente
3. Ative a Google Drive API

### 2. Configurar OAuth 2.0
1. No Google Cloud Console, vá para "APIs & Services" > "Credentials"
2. Clique em "Create Credentials" > "OAuth 2.0 Client IDs"
3. Configure para aplicação Android
4. Adicione o SHA-1 fingerprint do seu certificado de assinatura

### 3. Baixar google-services.json
1. No Google Cloud Console, vá para "Project Settings"
2. Baixe o arquivo `google-services.json`
3. Substitua o arquivo atual em `app/google-services.json`

### 4. Configurar Permissões
O aplicativo solicita as seguintes permissões:
- `https://www.googleapis.com/auth/drive.file` - Acesso a arquivos criados pelo app

## Funcionalidades Implementadas

### Autenticação
- Google Sign-In integrado
- Verificação de autenticação existente
- Tratamento de erros de autenticação

### Upload de Arquivos
- Upload de gravações para o Google Drive
- Progresso de upload em tempo real
- Tratamento de erros de rede

### Gerenciamento de Arquivos
- Listagem de arquivos no Google Drive
- Exclusão de arquivos
- Geração de URLs de download

### Armazenamento Local
- Cache de informações de autenticação
- Sincronização automática

## Estrutura de Pastas no Google Drive

O aplicativo cria automaticamente uma pasta chamada "EyeDock Recordings" no Google Drive do usuário, onde todas as gravações são armazenadas.

## Logs e Debug

Para monitorar o funcionamento, use:
```bash
adb logcat -s EyeDock CloudBackupViewModel GoogleDriveStorage
```

## Problemas Comuns

### 1. Erro de Autenticação
- Verifique se o `google-services.json` está correto
- Confirme se o SHA-1 fingerprint está configurado
- Verifique se a Google Drive API está ativada

### 2. Erro de Permissões
- O usuário deve conceder permissão para acessar o Google Drive
- Verifique se a conta Google está logada no dispositivo

### 3. Erro de Rede
- Verifique a conexão com a internet
- Confirme se o dispositivo pode acessar as APIs do Google

## Desenvolvimento vs Produção

### Desenvolvimento
- Usa configuração mock para testes rápidos
- Não requer configuração real do Google Cloud
- Funcionalidades simuladas

### Produção
- Usa Google Drive real
- Requer configuração completa no Google Cloud Console
- Funcionalidades completas de backup

## Próximos Passos

1. Configure um projeto real no Google Cloud Console
2. Obtenha o arquivo `google-services.json` real
3. Teste a autenticação e upload
4. Configure monitoramento e logs de produção
