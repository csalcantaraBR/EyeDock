# EyeDock

Aplicativo Android para conexão e gerenciamento de câmeras IP.

## Funcionalidades

- Conexão com câmeras IP via RTSP
- Suporte a protocolo ONVIF
- Interface moderna com Jetpack Compose
- Testes automatizados configurados
- CI/CD com GitHub Actions

## Testes

O projeto possui testes automatizados configurados:

- **Testes Unitários**: Executados via Gradle
- **GitHub Actions**: Workflows para CI/CD
- **Cobertura**: Relatórios JaCoCo

## Desenvolvimento

```bash
# Executar testes
./gradlew :app:testDebugUnitTest

# Gerar relatório de cobertura
./gradlew jacocoTestReport
```

## Status dos Workflows

✅ Testes automatizados configurados e funcionando
✅ GitHub Actions prontos para execução
