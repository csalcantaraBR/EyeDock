# EyeDock - Escopo de Testes Automatizados (TDD)

## Visão Geral

Este documento define o escopo completo de testes automatizados para o projeto EyeDock, seguindo rigorosamente o modelo TDD (Test-Driven Development). O projeto é um gerenciador de câmeras IP Android com suporte a Yoosee/ONVIF/RTSP, pronto para Play Store.

## Metodologia TDD

### Ciclo RED → GREEN → REFACTOR

1. **RED**: Escrever testes que falham primeiro
2. **GREEN**: Implementar código mínimo para passar os testes
3. **REFACTOR**: Melhorar o design mantendo todos os testes verdes

### Regras de Qualidade

- **Cobertura**: Unit ≥ 85%, Instrumentation ≥ 70%
- **Performance**: Latência live p50 ≤ 1.0s, p95 ≤ 1.8s
- **Conexão RTSP**: ≤ 2s ou erro claro
- **Recording throughput**: ≥ 5 MB/s sustentado
- **ANR/Crash-free**: UI sem ANR em wall, playback, settings
- **Acessibilidade**: TalkBack + navegação por teclado

## Estrutura de Módulos de Teste

```
app/
├── src/test/           # Unit tests (JUnit5)
├── src/androidTest/    # Instrumentation tests (Espresso)
└── src/benchmark/      # Performance tests (Macrobenchmark)

core/
├── onvif/src/test/     # ONVIF unit tests
├── media/src/test/     # Media streaming unit tests
└── storage/src/test/   # Storage SAF unit tests
```

## 1. Discovery & Streams

### 1.1 OnvifDiscoveryTest
**Objetivo**: Descoberta de dispositivos ONVIF na rede local

```kotlin
@Test
fun `deve descobrir dispositivos ONVIF na rede local`()

@Test  
fun `deve enumerar servicos Media Events PTZ quando disponivel`()

@Test
fun `deve falhar graciosamente quando nenhum dispositivo encontrado`()

@Test
fun `deve timeout discovery apos 10 segundos`()
```

### 1.2 RtspConnectTest  
**Objetivo**: Conexão RTSP com fallback

```kotlin
@Test
fun `deve conectar stream onvif1 em menos de 2 segundos`()

@Test
fun `deve fazer fallback para onvif2 quando onvif1 falha`()

@Test
fun `deve retornar erro claro quando ambos streams falham`()

@Test
fun `deve manter conexao estavel por pelo menos 5 minutos`()
```

### 1.3 StreamProfileTest
**Objetivo**: Detecção de perfis de stream

```kotlin
@Test  
fun `deve detectar perfil 1080p como stream principal`()

@Test
fun `deve detectar sub-stream quando disponivel`()

@Test
fun `deve validar codec suportado pelo ExoPlayer`()
```

### 1.4 NightVisionStateTest
**Objetivo**: Controle de visão noturna

```kotlin
@Test
fun `deve ler estado atual da visao noturna quando exposto`()

@Test
fun `deve alternar visao noturna quando suportado`()

@Test
fun `deve pular teste quando visao noturna nao disponivel`()
```

## 2. Events: Motion & Sound

### 2.1 OnvifEventsSubscriptionTest
**Objetivo**: Subscrição e recebimento de eventos ONVIF

```kotlin
@Test
fun `deve subscrever eventos de movimento via ONVIF`()

@Test
fun `deve receber notificacao de movimento em tempo real`()

@Test
fun `deve manter subscricao ativa durante reconexoes`()

@Test
fun `deve unsubscribe limpo ao desconectar camera`()
```

### 2.2 SoundEventTest
**Objetivo**: Detecção de eventos sonoros

```kotlin
@Test
fun `deve receber evento de som acima do threshold configurado`()

@Test
fun `deve simular evento sonoro para teste`()

@Test
fun `deve configurar sensibilidade de deteccao sonora`()
```

### 2.3 EventLatencyTest
**Objetivo**: Latência de eventos

```kotlin
@Test
fun `latencia de eventos deve ser p95 menor que 2 segundos`()

@Test
fun `deve medir tempo entre evento real e notificacao`()
```

## 3. PTZ & Auto-Tracking

### 3.1 PtzCapabilitiesTest
**Objetivo**: Verificação de capacidades PTZ

```kotlin
@Test
fun `deve confirmar operacoes PTZ quando anunciadas pelo dispositivo`()

@Test
fun `deve enumerar limites de movimento Pan Tilt Zoom`()

@Test
fun `deve validar velocidades de movimento suportadas`()
```

### 3.2 PtzMoveTest
**Objetivo**: Movimentos PTZ

```kotlin
@Test
fun `movimentos relativos devem atualizar posicao dentro dos limites`()

@Test
fun `deve parar movimento ao atingir limite fisico`()

@Test
fun `deve retornar posicao atual apos movimento`()
```

### 3.3 AutoTrackingToggleTest
**Objetivo**: Controle de auto-tracking

```kotlin
@Test
fun `toggle ON OFF deve refletir no status do dispositivo`()

@Test
fun `auto tracking deve influenciar deltas de movimento`()

@Test
fun `deve desabilitar controle manual quando auto tracking ativo`()
```

### 3.4 NoPtzSkipTest
**Objetivo**: Tratamento quando PTZ não disponível

```kotlin
@Test
fun `deve marcar teste como skipped com razao quando PTZ ausente`()

@Test
fun `deve ocultar controles PTZ na UI quando nao suportado`()
```

## 4. Two-Way Audio

### 4.1 MicCaptureTest
**Objetivo**: Captura de áudio do microfone

```kotlin
@Test
fun `deve validar sample rate e channels para microfone`()

@Test
fun `deve solicitar permissao de microfone antes de capturar`()

@Test
fun `deve capturar audio com qualidade adequada para transmissao`()
```

### 4.2 TalkbackTest
**Objetivo**: Transmissão de áudio para câmera

```kotlin
@Test
fun `deve enviar audio curto para camera e verificar ack`()

@Test
fun `deve testar loopback de audio quando suportado`()

@Test
fun `deve funcionar apenas com hold to talk nao background`()
```

## 5. User-Selectable Storage (SAF)

### 5.1 StoragePickerTest
**Objetivo**: Seleção de storage via SAF

```kotlin
@Test
fun `usuario deve selecionar pasta drive e persistir URI permissions`()

@Test
fun `permissions devem persistir atraves de reboots`()

@Test
fun `deve suportar SD externo USB OTG e network providers`()
```

### 5.2 WriteSegmentTest
**Objetivo**: Escrita de segmentos de vídeo

```kotlin
@Test
fun `deve escrever mp4 e metadata na raiz escolhida`()

@Test
fun `deve verificar escrita via DocumentFile SAF`()

@Test
fun `deve criar estrutura CameraName yyyy-MM-dd HHmmss mp4`()
```

### 5.3 QuotaPolicyTest
**Objetivo**: Políticas de retenção e quota

```kotlin
@Test
fun `deve enforcar retencao por tamanho ou dias`()

@Test
fun `deve fazer delecao segura mantendo indice consistente`()

@Test
fun `deve alertar quando espaco insuficiente`()
```

### 5.4 PermissionRevokedTest
**Objetivo**: Tratamento de permissões revogadas

```kotlin
@Test
fun `app deve lidar com URI revogada graciosamente`()

@Test
fun `deve prompts re grant sem perda de dados`()

@Test
fun `deve manter cache local temporario durante re grant`()
```

### 5.5 ExportShareTest
**Objetivo**: Exportação e compartilhamento

```kotlin
@Test
fun `deve compartilhar snapshot clip via SAF share sheet`()

@Test
fun `deve exportar para localizacao escolhida pelo usuario`()
```

## 6. Recording & Playback

### 6.1 ContinuousRecordTest
**Objetivo**: Gravação contínua

```kotlin
@Test
fun `segmentos de N minutos devem fazer roll automatico`()

@Test
fun `indice deve aparecer na timeline apos gravacao`()

@Test
fun `deve manter gravacao durante reconexoes temporarias`()
```

### 6.2 MotionRecordTest
**Objetivo**: Gravação por movimento

```kotlin
@Test
fun `movimento deve triggerar start stop da gravacao`()

@Test
fun `gaps devem ser trimmed automaticamente`()

@Test
fun `deve pre buffer X segundos antes do evento`()
```

### 6.3 PlaybackSeekTest
**Objetivo**: Controles de playback

```kotlin
@Test
fun `timeline seek deve ser preciso ao frame`()

@Test
fun `controle de velocidade deve funcionar 0 5x 1x 2x 4x`()

@Test
fun `snapshot durante playback deve ser frame accurate`()
```

## 7. Health & Telemetry

### 7.1 HealthMonitorTest
**Objetivo**: Monitoramento de saúde das câmeras

```kotlin
@Test
fun `deve monitorar status online offline bitrate frames dropped`()

@Test
fun `deve registrar timestamp de last seen`()

@Test
fun `deve detectar cameras que ficaram offline`()
```

### 7.2 ReconnectBackoffTest
**Objetivo**: Reconexão com backoff

```kotlin
@Test
fun `Wi Fi drop deve retry com exponential backoff`()

@Test
fun `downtime por minuto deve ser menor que 2s com 1 porcent loss`()

@Test
fun `deve limitar numero maximo de tentativas`()
```

## 8. Security & Policy

### 8.1 RuntimePermissionTest
**Objetivo**: Permissões de runtime

```kotlin
@Test
fun `deve solicitar camera mic notifications storage via SAF apenas`()

@Test
fun `nao deve usar raw paths apenas SAF`()

@Test
fun `deve degradar funcionalidade graciosamente sem permissoes`()
```

### 8.2 DataSafetyTest
**Objetivo**: Segurança de dados

```kotlin
@Test
fun `nao deve logar informacoes sensitivas`()

@Test
fun `diagnostics devem ser opt in pelo usuario`()

@Test
fun `privacy text deve estar surfaced na UI`()
```

### 8.3 RbacTest (Opcional)
**Objetivo**: Controle de acesso baseado em roles

```kotlin
@Test
fun `roles admin operator viewer devem restringir acoes`()

@Test
fun `viewer nao deve poder adicionar cameras ou mudar settings`()
```

## 9. UI/Accessibility/Performance

### 9.1 WallViewFpsTest
**Objetivo**: Performance da visualização em grid

```kotlin
@Test
fun `grid 2x2 3x3 deve manter smoothness via macrobenchmark`()

@Test
fun `deve manter 60fps com multiplas streams simultaneas`()

@Test
fun `memory usage deve permanecer estavel durante long runs`()
```

### 9.2 StartupTimeTest
**Objetivo**: Tempo de inicialização

```kotlin
@Test
fun `cold start deve ficar abaixo do target definido`()

@Test
fun `baseline profile regression check deve passar`()

@Test
fun `warm start deve ser instantaneo`()
```

### 9.3 AccessibilityChecks
**Objetivo**: Acessibilidade

```kotlin
@Test
fun `labels content descriptions devem estar presentes`()

@Test
fun `focus order deve ser logico para teclado navegacao`()

@Test
fun `contrast deve passar verificacao automatica`()

@Test
fun `tamanhos de toque devem ser maiores que 48dp`()
```

## 10. E2E (End-to-End)

### 10.1 AddCameraFlow
**Objetivo**: Fluxo completo de adição de câmera

```kotlin
@Test
fun `fluxo completo discover add creds live view deve funcionar`()

@Test
fun `QR scan deve preencher formulario automaticamente`()

@Test
fun `validacao de credenciais deve funcionar antes de salvar`()
```

### 10.2 AlertFlow
**Objetivo**: Fluxo de alertas

```kotlin
@Test
fun `motion event deve gerar push notification`()

@Test
fun `abrir notification deve levar para recording no timestamp`()

@Test
fun `deve manter historico de alertas por tempo configurado`()
```

### 10.3 TalkAndMoveFlow
**Objetivo**: Fluxo integrado de comunicação e movimento

```kotlin
@Test
fun `hold to talk deve enviar audio out`()

@Test
fun `PTZ move durante talkback deve funcionar simultaneamente`()

@Test
fun `snapshot durante operacao deve salvar no drive escolhido`()
```

## Test Data & Mocks

### Dados Sintéticos
- **Sample streams**: streams RTSP sintéticos para teste offline
- **Mock cameras**: dispositivos virtuais com capacidades variadas
- **Synthetic events**: eventos de movimento/som simulados
- **Performance baselines**: métricas de referência para regressão

### Ambientes de Teste
- **Local**: emulador Android com mock servers
- **CI**: GitHub Actions com matrix de API levels
- **Device farm**: testes em dispositivos reais para validação final

## CI/CD Pipeline

### Gates de Qualidade
1. **Unit Tests**: deve passar 100%
2. **Coverage**: unit ≥ 85%, instrumentation ≥ 70%
3. **Lint**: detekt/ktlint sem violações
4. **Performance**: benchmarks não devem regredir
5. **Security**: scan de vulnerabilidades em dependências

### Artefatos de Release
- AAB assinado para Play Store
- Mapas de Proguard/R8
- Baseline profiles
- Screenshots para store listing
- Privacy Policy atualizada

## Execução de Testes

### Scripts Disponíveis
```bash
./gradlew test                    # Unit tests
./gradlew connectedCheck         # Instrumentation tests  
./gradlew :app:benchmarkRelease  # Performance tests
./gradlew lint detekt ktlint     # Static analysis
./gradlew ci:all                 # Pipeline completa
```

### Métricas de Sucesso
- **Tempo de execução**: suite completa < 15 minutos
- **Flakiness**: taxa de falso positivo < 1%
- **Manutenibilidade**: tempo para adicionar novo teste < 30 minutos
- **Feedback**: desenvolvedor recebe resultado em < 5 minutos para smoke tests

## Considerações Especiais

### Dispositivos Sem Capacidades
- Testes devem usar `assumeTrue()` para skip com razão clara
- UI deve ocultar controles não disponíveis
- Documentação deve listar capacidades mínimas

### Ambientes de Rede Variados
- Testes devem simular condições de rede instável
- Timeout configuráveis para diferentes cenários
- Fallback gracioso quando serviços indisponíveis

### Conformidade Play Store
- Privacy Policy implementada e linkada
- Data Safety form preenchida corretamente
- Permissões justificadas e documentadas
- Content rating apropriado para público alvo

---

**Nota**: Este documento deve ser atualizado conforme o desenvolvimento progride, sempre mantendo os testes como especificação viva do comportamento esperado do sistema.
