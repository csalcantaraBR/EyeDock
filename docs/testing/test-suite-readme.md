# 🧪 EyeDock - Suite de Testes Automatizados

## 📋 Visão Geral

Esta suite de testes automatizados foi desenvolvida seguindo rigorosamente a metodologia **TDD (Test-Driven Development)** para garantir a qualidade, estabilidade e performance do projeto EyeDock. A suite abrange testes unitários, funcionais, de integração e de regressão.

## 🎯 Objetivos

- **Cobertura**: Unit ≥ 85%, Integration ≥ 70%
- **Performance**: Latência p50 ≤ 1.0s, p95 ≤ 1.8s
- **Estabilidade**: Conexão RTSP ≤ 2s ou erro claro
- **Qualidade**: Zero ANR/Crash em testes de UI
- **Acessibilidade**: 100% dos controles com labels

## 🏗️ Arquitetura da Suite

```
test-suite/
├── app/src/test/kotlin/           # Testes unitários da aplicação
│   └── com/eyedock/app/
│       ├── domain/                # Testes de domínio
│       ├── security/              # Testes de segurança
│       └── utils/                 # Testes de utilitários
├── core/*/src/test/kotlin/        # Testes dos módulos core
│   ├── onvif/                     # Testes ONVIF
│   ├── media/                     # Testes de streaming
│   ├── storage/                   # Testes de storage
│   └── events/                    # Testes de eventos
├── app/src/androidTest/kotlin/    # Testes de instrumentação
├── test-scripts/                  # Scripts de automação
└── .github/workflows/             # CI/CD pipeline
```

## 📊 Categorias de Testes

### 1. 🧩 Testes Unitários (FAST_TEST)
**Objetivo**: Testar componentes isolados
- **Tempo**: < 1s cada
- **Execução**: Frequentemente durante desenvolvimento
- **Cobertura**: ≥ 85%

```kotlin
@Test
@Tag(FAST_TEST)
@Tag(SMOKE_TEST)
fun `deve criar conexao valida com dados basicos`() {
    // Teste unitário rápido
}
```

### 2. 🔗 Testes Funcionais (INTEGRATION_TEST)
**Objetivo**: Testar interação entre componentes
- **Tempo**: 1-10s cada
- **Execução**: Antes de commits
- **Cobertura**: ≥ 70%

```kotlin
@Test
@Tag(INTEGRATION_TEST)
@Tag(ONVIF_TEST)
fun `deve descobrir dispositivos ONVIF na rede local`() {
    // Teste de integração
}
```

### 3. 🔄 Testes de Regressão (REGRESSION_TEST)
**Objetivo**: Garantir estabilidade e performance
- **Tempo**: 10-60s cada
- **Execução**: Builds de release
- **Foco**: Estabilidade e performance

```kotlin
@Test
@Tag(REGRESSION_TEST)
@Tag(PERFORMANCE_TEST)
fun `deve manter latencia p50 menor igual 1s p95 menor igual 1.8s`() {
    // Teste de regressão de performance
}
```

### 4. 🎯 Testes de Smoke (SMOKE_TEST)
**Objetivo**: Validação rápida de funcionalidades críticas
- **Tempo**: < 30s total
- **Execução**: Antes de cada deploy
- **Foco**: Funcionalidades essenciais

## 🚀 Execução dos Testes

### Via Gradle

```bash
# Executar todos os testes
./gradlew test

# Executar testes por categoria
./gradlew test --tests "*Unit*"
./gradlew test --tests "*Functional*"
./gradlew test --tests "*Regression*"

# Executar testes por tag
./gradlew test -Dgroups="fast,smoke"
./gradlew test -Dgroups="integration,onvif"
```

### Via Script PowerShell

```powershell
# Executar suite completa
.\test-scripts\run-test-suite.ps1

# Executar tipos específicos
.\test-scripts\run-test-suite.ps1 -TestType unit
.\test-scripts\run-test-suite.ps1 -TestType functional
.\test-scripts\run-test-suite.ps1 -TestType regression
.\test-scripts\run-test-suite.ps1 -TestType integration
.\test-scripts\run-test-suite.ps1 -TestType smoke

# Com relatório detalhado
.\test-scripts\run-test-suite.ps1 -TestType all -GenerateReport -Verbose
```

### Via GitHub Actions

```yaml
# Executar manualmente
# Vá para Actions > EyeDock Test Suite > Run workflow
# Selecione o tipo de teste desejado
```

## 📈 Métricas de Qualidade

### Performance
- **Latência RTSP**: p50 ≤ 1.0s, p95 ≤ 1.8s
- **Conexão**: ≤ 2s ou erro claro
- **Throughput**: ≥ 5MB/s sustentado
- **CPU**: < 30% durante streaming

### Estabilidade
- **Reconexão**: Automática após falhas
- **Memória**: Sem vazamentos após múltiplas conexões
- **Concorrência**: Thread-safe para operações simultâneas

### Cobertura
- **Unit Tests**: ≥ 85%
- **Integration Tests**: ≥ 70%
- **Critical Paths**: 100%

## 🔧 Configuração

### Dependências de Teste

```kotlin
dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    
    // Mockito
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    
    // Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    
    // Truth Assertions
    testImplementation("com.google.truth:truth:1.1.5")
    
    // AndroidX Test
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.3")
}
```

### Configuração do Gradle

```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    
    // Configurar timeouts
    timeout.set(Duration.ofMinutes(10))
    
    // Configurar paralelização
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}
```

## 📋 Testes Implementados

### 1. 📡 ONVIF Discovery
- ✅ Descoberta de dispositivos na rede
- ✅ Enumeração de serviços (Media/Events/PTZ)
- ✅ Timeout de discovery (10s)
- ✅ Validação de formato de subnet
- ✅ WS-Discovery protocol

### 2. 📺 RTSP Streaming
- ✅ Conexão estável por 5+ minutos
- ✅ Reconexão automática após falhas
- ✅ Múltiplas conexões simultâneas
- ✅ Performance benchmarks
- ✅ Tratamento de erros

### 3. 💾 Storage Management
- ✅ Integração SAF e escrita de arquivos
- ✅ Políticas de retenção
- ✅ Compartilhamento de arquivos
- ✅ Recuperação de operações interrompidas
- ✅ Operações concorrentes

### 4. 🔐 Security
- ✅ Validação de credenciais
- ✅ Criptografia de dados sensíveis
- ✅ Tratamento de URIs revogadas
- ✅ Compliance com políticas de privacidade

## 🎯 Gates de Qualidade

### Build Gates
1. **Unit Tests**: 100% pass
2. **Coverage**: Unit ≥ 85%, Integration ≥ 70%
3. **Static Analysis**: Detekt/KtLint sem violações
4. **Security**: Scan de vulnerabilidades em dependências

### Performance Gates
1. **Latência**: p50 ≤ 1.0s, p95 ≤ 1.8s
2. **Throughput**: ≥ 5MB/s sustentado
3. **Memory**: Sem vazamentos detectados
4. **CPU**: < 30% durante operações normais

### Quality Gates
1. **Flakiness**: < 1% falsos positivos
2. **ANR/Crash**: 0 em testes de UI
3. **Accessibility**: 100% dos controles com labels

## 📊 Relatórios

### Relatório HTML
```powershell
.\test-scripts\run-test-suite.ps1 -GenerateReport
```
Gera relatório HTML com:
- Resumo executivo
- Métricas por categoria
- Gráficos de performance
- Próximos passos

### Relatório GitHub Actions
- Comentários automáticos em PRs
- Artefatos de teste para download
- Notificações Slack
- Quality gates report

## 🔄 CI/CD Pipeline

### Triggers
- **Push**: Branches main/develop
- **PR**: Qualquer pull request
- **Schedule**: Testes de regressão diários (2h)
- **Manual**: Via workflow dispatch

### Jobs
1. **Unit Tests**: Testes unitários rápidos
2. **Functional Tests**: Testes de integração
3. **Regression Tests**: Testes de estabilidade
4. **Performance Tests**: Benchmarks
5. **Android Tests**: Instrumentação
6. **Quality Gates**: Análise estática + segurança
7. **Test Report**: Geração de relatórios
8. **Notify**: Notificações de resultado

## 🛠️ Troubleshooting

### Problemas Comuns

#### Testes Falhando
```bash
# Verificar logs detalhados
./gradlew test --info --stacktrace

# Executar teste específico
./gradlew test --tests "com.eyedock.app.domain.CameraConnectionTest"

# Verificar cobertura
./gradlew jacocoTestReport
```

#### Performance Degradada
```bash
# Executar testes de performance
./gradlew test --tests "*Performance*"

# Verificar métricas de memória
./gradlew test --tests "*Memory*"
```

#### Problemas de Rede
```bash
# Executar testes de rede
./gradlew test --tests "*Network*"

# Verificar conectividade
./gradlew test --tests "*Connection*"
```

### Logs e Debugging

```kotlin
// Adicionar logs em testes
@Test
fun `teste com logs`() {
    println("🔍 Iniciando teste...")
    // ... teste ...
    println("✅ Teste concluído")
}
```

## 📚 Próximos Passos

### Melhorias Planejadas
1. **Testes E2E**: Fluxos completos de usuário
2. **Testes de Stress**: Carga alta e edge cases
3. **Testes de Acessibilidade**: TalkBack e navegação por teclado
4. **Testes de Localização**: Múltiplos idiomas
5. **Testes de Compatibilidade**: Diferentes versões Android

### Monitoramento
1. **Métricas em Produção**: Latência real, throughput
2. **Alertas**: Falhas de teste, degradação de performance
3. **Dashboards**: Visualização de tendências
4. **Relatórios**: Semanais/mensais de qualidade

## 🤝 Contribuição

### Adicionando Novos Testes

1. **Seguir nomenclatura**: `deve_acao_quando_condicao`
2. **Usar tags apropriadas**: `@Tag(FAST_TEST)`, `@Tag(INTEGRATION_TEST)`
3. **Documentar cenário**: `@DisplayName("Descrição clara")`
4. **Implementar TDD**: RED → GREEN → REFACTOR

### Exemplo de Novo Teste

```kotlin
@Test
@Tag(FAST_TEST)
@Tag(SMOKE_TEST)
@DisplayName("Deve validar novo formato de QR code")
fun `deve validar novo formato de QR code`() {
    // RED: Teste que falha primeiro
    val qrCode = "novo:formato:qr:code"
    val validator = QrCodeValidator()
    
    // Act & Assert
    assertTrue(validator.isValid(qrCode))
}
```

## 📞 Suporte

Para dúvidas sobre a suite de testes:
- 📧 Email: testes@eyedock.com
- 💬 Slack: #eyedock-tests
- 📖 Wiki: [Test Suite Documentation](https://github.com/eyedock/docs/tests)

---

**Lembre-se**: Em TDD, o teste é a especificação. Se o teste não existir, a funcionalidade não existe.
