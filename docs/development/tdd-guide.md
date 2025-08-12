# EyeDock - Guia de Implementação TDD

## Objetivo

Este guia define como implementar o projeto EyeDock seguindo rigorosamente o modelo TDD (Test-Driven Development), garantindo qualidade, cobertura e conformidade com os requisitos especificados.

## Metodologia TDD

### Ciclo RED → GREEN → REFACTOR

#### 1. RED (Escrever teste que falha)
```kotlin
@Test
@Tag(FAST_TEST)
@DisplayName("Deve descobrir dispositivos ONVIF na rede local")
fun `deve descobrir dispositivos ONVIF na rede local`() {
    // Arrange
    val discoveryService = OnvifDiscoveryService() // NÃO EXISTE AINDA
    val subnet = "192.168.0.0/24"
    
    // Act & Assert - DEVE FALHAR
    val devices = discoveryService.discover(subnet)
    assertTrue(devices.isNotEmpty())
}
```

#### 2. GREEN (Implementar código mínimo)
```kotlin
class OnvifDiscoveryService {
    fun discover(subnet: String): List<OnvifDevice> {
        // Implementação mínima para passar o teste
        return listOf(OnvifDevice("192.168.0.100", "Mock Camera"))
    }
}
```

#### 3. REFACTOR (Melhorar design)
```kotlin
class OnvifDiscoveryService(
    private val networkScanner: NetworkScanner,
    private val onvifClient: OnvifClient
) {
    suspend fun discover(subnet: String): List<OnvifDevice> {
        return networkScanner.scanSubnet(subnet)
            .mapNotNull { ip -> onvifClient.probe(ip) }
    }
}
```

## Sequência de Implementação

### Fase 1: Fundações (Semana 1-2)

#### 1.1 Setup do Projeto
```bash
# Criar estrutura de módulos
mkdir -p app/src/{main,test,androidTest}/kotlin
mkdir -p core/{onvif,media,storage}/src/{main,test}/kotlin

# Configurar dependências de teste
./gradlew test # DEVE FALHAR - ainda não há testes
```

#### 1.2 Discovery básico (RED → GREEN → REFACTOR)
```kotlin
// RED: Teste que falha
class OnvifDiscoveryTest {
    @Test fun `deve encontrar camera na rede local`() {
        val service = OnvifDiscoveryService()
        val devices = service.discover("192.168.1.0/24")
        assertTrue(devices.isNotEmpty())
    }
}

// GREEN: Implementação mínima
class OnvifDiscoveryService {
    fun discover(subnet: String) = listOf(mockDevice())
}

// REFACTOR: Implementação real
class OnvifDiscoveryService {
    suspend fun discover(subnet: String): Flow<OnvifDevice> = flow {
        // Implementação real com coroutines
    }
}
```

### Fase 2: Streams & Conexão (Semana 3-4)

#### 2.1 RTSP Connection (RED → GREEN → REFACTOR)
```kotlin
// RED
@Test fun `deve conectar stream RTSP em menos de 2 segundos`() {
    val client = RtspClient()
    val start = currentTimeMillis()
    client.connect("rtsp://192.168.1.100/onvif1")
    val elapsed = currentTimeMillis() - start
    assertTrue(elapsed < 2000)
}

// GREEN: Mock que "conecta" instantaneamente
class RtspClient {
    fun connect(url: String) { /* mock connection */ }
}

// REFACTOR: ExoPlayer integration
class RtspClient(private val exoPlayer: ExoPlayer) {
    suspend fun connect(url: String): ConnectionResult {
        // Implementação real com timeout
    }
}
```

#### 2.2 Stream Fallback (RED → GREEN → REFACTOR)
```kotlin
// RED
@Test fun `deve tentar onvif2 quando onvif1 falha`() {
    val manager = StreamManager()
    val result = manager.connectWithFallback(cameraConfig)
    assertEquals("/onvif2", result.successfulPath)
}

// GREEN: Fallback hardcoded
class StreamManager {
    fun connectWithFallback(config: CameraConfig): ConnectionResult {
        return ConnectionResult(true, "/onvif2")
    }
}

// REFACTOR: Lógica real de fallback
class StreamManager {
    suspend fun connectWithFallback(config: CameraConfig): ConnectionResult {
        val paths = listOf("/onvif1", "/onvif2")
        for (path in paths) {
            try {
                rtspClient.connect("${config.rtspBase}$path")
                return ConnectionResult(true, path)
            } catch (e: ConnectionException) {
                continue
            }
        }
        return ConnectionResult(false, null)
    }
}
```

### Fase 3: Storage SAF (Semana 5)

#### 3.1 Storage Selection (RED → GREEN → REFACTOR)
```kotlin
// RED
@Test fun `deve persistir URI permission apos reinicio`() {
    val storage = StorageManager(context)
    storage.selectStorage(mockUri)
    storage.clearCache() // simular restart
    assertTrue(storage.hasValidPermission())
}

// GREEN: Mock persistente
class StorageManager {
    private var hasPerm = false
    fun selectStorage(uri: Uri) { hasPerm = true }
    fun hasValidPermission() = hasPerm
}

// REFACTOR: SAF real
class StorageManager(private val context: Context) {
    fun selectStorage(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
    
    fun hasValidPermission(): Boolean {
        return context.contentResolver.persistedUriPermissions.isNotEmpty()
    }
}
```

### Fase 4: Events & PTZ (Semana 6-7)

#### 4.1 Motion Events (RED → GREEN → REFACTOR)
```kotlin
// RED
@Test fun `deve receber evento movimento em menos de 2s`() {
    val eventManager = EventManager()
    val events = mutableListOf<MotionEvent>()
    eventManager.subscribeMotion { events.add(it) }
    
    triggerMotion() // mock
    delay(2000)
    assertTrue(events.isNotEmpty())
}

// GREEN: Mock events
class EventManager {
    fun subscribeMotion(callback: (MotionEvent) -> Unit) {
        // Mock que chama callback imediatamente
        callback(MotionEvent(timestamp = now()))
    }
}

// REFACTOR: ONVIF subscription real
class EventManager(private val onvifClient: OnvifClient) {
    suspend fun subscribeMotion(callback: (MotionEvent) -> Unit) {
        onvifClient.subscribeEvents()
            .filterIsInstance<MotionEvent>()
            .collect(callback)
    }
}
```

### Fase 5: UI & E2E (Semana 8-9)

#### 5.1 QR Scanner (RED → GREEN → REFACTOR)
```kotlin
// RED
@Test fun `deve parsear QR code Yoosee corretamente`() {
    val scanner = QrScanner()
    val qrContent = """{"ip":"192.168.1.100","user":"admin",...}"""
    val config = scanner.parseQr(qrContent)
    assertEquals("192.168.1.100", config.ip)
}

// GREEN: Parser básico
class QrScanner {
    fun parseQr(content: String): CameraConfig {
        return CameraConfig(ip = "192.168.1.100") // hardcoded
    }
}

// REFACTOR: JSON parsing real
class QrScanner {
    fun parseQr(content: String): CameraConfig {
        val json = Json.decodeFromString<QrPayload>(content)
        return CameraConfig(
            ip = json.ip,
            user = json.user,
            // ... mapeamento completo
        )
    }
}
```

## Regras de Qualidade

### Gates Obrigatórios

1. **Cobertura de Código**
   ```kotlin
   // build.gradle
   jacocoTestCoverageVerification {
       violationRules {
           rule {
               limit {
                   minimum = 0.85 // 85% unit tests
               }
           }
       }
   }
   ```

2. **Performance Benchmarks**
   ```kotlin
   @Test
   @Tag(PERFORMANCE_TEST)
   fun `latencia stream deve ser p95 menor que 1_8s`() {
       val measurements = measureLatency(100)
       val p95 = measurements.percentile(95)
       assertTrue(p95 <= 1800, "p95: ${p95}ms")
   }
   ```

3. **Acessibilidade**
   ```kotlin
   @Test
   @Tag(UI_TEST)
   fun `todos controles devem ter content description`() {
       composeTestRule.onAllNodes(hasClickAction())
           .assertAll(hasContentDescription())
   }
   ```

### CI Pipeline

```yaml
# .github/workflows/ci.yml
jobs:
  test:
    steps:
      - name: Unit Tests
        run: ./gradlew test
      - name: Coverage Gate
        run: ./gradlew jacocoTestCoverageVerification
      - name: Instrumentation Tests
        run: ./gradlew connectedCheck
      - name: Benchmarks
        run: ./gradlew benchmarkRelease
```

## Execução de Testes

### Scripts Disponíveis

```bash
# Testes rápidos (desenvolvimento)
./test-scripts/run-tests.sh unit

# Testes de integração
./test-scripts/run-tests.sh instrumentation

# Pipeline completa
./test-scripts/run-tests.sh all

# Apenas smoke tests
./gradlew smokeTest

# Testes por categoria
./gradlew test -PtestType=unit
./gradlew test -Dgroups="fast,smoke"
```

### Categorias de Teste

```kotlin
@Tag(FAST_TEST)      // < 1s cada, executado sempre
@Tag(SMOKE_TEST)     // Subset mínimo para validação
@Tag(INTEGRATION_TEST) // 1-10s, interação entre componentes
@Tag(E2E_TEST)       // Fluxos completos
@Tag(PERFORMANCE_TEST) // Benchmarks e métricas
```

## Tratamento de Dispositivos

### Capacidades Ausentes
```kotlin
@Test
fun `deve skip PTZ quando nao disponivel`() {
    val camera = mockCameraWithoutPtz()
    Assumptions.assumeTrue(camera.hasPtz(), "PTZ não disponível")
    
    // Teste só executa se PTZ disponível
    val controller = PtzController(camera)
    controller.moveRelative(10f, 5f)
}
```

### Mock vs Real
```kotlin
class OnvifClientTest {
    
    @Test
    @Tag(UNIT_TEST)
    fun `test com mock device`() {
        val mockDevice = MockOnvifDevice()
        // teste unitário rápido
    }
    
    @Test
    @Tag(REAL_CAMERA_TEST)
    fun `test com camera real`() {
        val realIP = System.getProperty("test.camera.ip")
        assumeTrue(realIP != null, "Camera real não configurada")
        // teste de integração real
    }
}
```

## Estrutura de Pastas

```
app/
├── src/
│   ├── main/kotlin/               # Código principal
│   ├── test/kotlin/               # Unit tests
│   ├── androidTest/kotlin/        # Instrumentation tests
│   ├── testShared/kotlin/         # Código compartilhado entre testes
│   └── benchmark/kotlin/          # Performance tests
├── build.gradle.kts
└── proguard-rules.pro

core/
├── onvif/
│   ├── src/main/kotlin/
│   └── src/test/kotlin/
├── media/
│   ├── src/main/kotlin/
│   └── src/test/kotlin/
└── storage/
    ├── src/main/kotlin/
    └── src/test/kotlin/
```

## Métricas de Sucesso

### Desenvolvimento
- **Ciclo TDD**: RED → GREEN → REFACTOR em < 30min
- **Feedback**: Resultado de smoke tests em < 5min
- **Cobertura**: Unit ≥ 85%, Integration ≥ 70%

### Performance
- **Latência**: p50 ≤ 1.0s, p95 ≤ 1.8s
- **Conexão**: RTSP ≤ 2s
- **Throughput**: Recording ≥ 5MB/s

### Qualidade
- **Flakiness**: < 1% falsos positivos
- **ANR/Crash**: 0 em testes de UI
- **Acessibilidade**: 100% dos controles com labels

## Próximos Passos

1. **Executar pipeline**: `./test-scripts/run-tests.sh all` (deve falhar)
2. **Implementar primeiro teste**: OnvifDiscoveryTest (RED)
3. **Código mínimo**: Mock implementation (GREEN)
4. **Refatorar**: Implementação real (REFACTOR)
5. **Repetir ciclo**: Para próxima funcionalidade

## Recursos Adicionais

- **TESTS.md**: Especificação completa de todos os testes
- **test-scripts/**: Scripts de execução automatizada
- **gradle/test-config.gradle**: Configurações centralizadas
- **buildSrc/**: Categorias e constantes de teste

---

**Lembre-se**: Em TDD, o teste é a especificação. Se o teste não existir, a funcionalidade não existe.
