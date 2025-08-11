# 🔐 Configuração de Secrets para GitHub Actions

Este documento explica como configurar os secrets necessários para o pipeline de deploy do EyeDock.

## 📋 Secrets Necessários

### 🔑 Android Keystore
Para assinar o APK/AAB de release:

- **`ENCODED_KEYSTORE`**: Keystore codificado em base64
- **`KEYSTORE_PASSWORD`**: Senha do keystore
- **`KEY_ALIAS`**: Alias da chave
- **`KEY_PASSWORD`**: Senha da chave

### 🔥 Firebase App Distribution
Para deploy de staging:

- **`FIREBASE_APP_ID`**: ID do app no Firebase
- **`FIREBASE_SERVICE_ACCOUNT_KEY`**: Chave de serviço do Firebase (JSON)

### 📱 Google Play Store
Para deploy de produção:

- **`GOOGLE_PLAY_SERVICE_ACCOUNT_KEY`**: Chave de serviço do Google Play (JSON)

### 📊 Codecov
Para relatórios de cobertura:

- **`CODECOV_TOKEN`**: Token do Codecov (opcional)

## 🛠️ Como Configurar

### 1. Android Keystore

```bash
# Gerar keystore (se não existir)
keytool -genkey -v -keystore eyedock-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias eyedock-key

# Codificar em base64
base64 -i eyedock-release-key.jks | tr -d '\n'
```

### 2. Firebase Setup

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Crie um projeto ou use existente
3. Adicione um app Android
4. Vá em Project Settings > Service Accounts
5. Gere uma nova chave privada (JSON)
6. Copie o conteúdo do JSON para o secret

### 3. Google Play Console

1. Acesse [Google Play Console](https://play.google.com/console)
2. Vá em Setup > API Access
3. Crie uma nova Service Account
4. Baixe o JSON da chave
5. Copie o conteúdo para o secret

## 📍 Onde Configurar

1. Vá para o repositório no GitHub
2. Settings > Secrets and variables > Actions
3. Clique em "New repository secret"
4. Adicione cada secret com o nome e valor correspondente

## 🔒 Segurança

- ✅ Nunca commite secrets no código
- ✅ Use sempre variáveis de ambiente
- ✅ Rotacione as chaves regularmente
- ✅ Use permissões mínimas necessárias
- ✅ Monitore o uso dos secrets

## 🧪 Teste Local

Para testar localmente sem expor secrets:

```bash
# Testar build sem assinatura
./gradlew assembleDebug

# Testar testes unitários
./gradlew :app:testDebugUnitTest

# Testar lint
./gradlew lint
```

## 📞 Suporte

Se precisar de ajuda com a configuração:

1. Verifique a documentação oficial
2. Consulte os logs do GitHub Actions
3. Teste localmente primeiro
4. Use o modo debug se necessário
