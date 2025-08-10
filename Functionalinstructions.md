# functionalinstructions.md

**Projeto:** EyeDock — Visualização e controle de câmeras IP via conexão direta (LAN) usando RTSP/ONVIF.

**Objetivo:** Implementar, no app mobile, fluxo completo de onboarding e visualização **sem dependência de nuvem/provedor**, suportando:

* Leitura de **QR Codes** em múltiplos formatos.
* **Normalização** das informações para um modelo único de conexão direta.
* **Descoberta ONVIF** (WS‑Discovery) para localizar câmeras na LAN.
* Obtenção de **RTSP Stream URI** e **perfis** ONVIF.
* **Player RTSP** com baixa latência (H.264/H.265).
* **PTZ** básico via ONVIF (quando disponível).
* Armazenamento **seguro** de credenciais.

---

## 1. Escopo & Requisitos

### 1.1. Escopo

* Android (Kotlin). iOS é opcional nesta fase (documentar interfaces para futura paridade).
* Conexão direta somente (LAN). Acesso remoto/NAT traversal **fora de escopo** neste documento.

### 1.2. Requisitos Funcionais (RF)

1. **RF-QR-Parse:** App deve ler QR Codes contendo: (a) URL RTSP, (b) JSON com campos de câmera, (c) UID/serial proprietário.
2. **RF-Normalize:** Converter qualquer payload lido em um objeto **CameraConnection** padronizado.
3. **RF-Discover:** Para payloads sem IP/RTSP, executar **WS‑Discovery** → ONVIF → `GetProfiles`/`GetStreamUri`.
4. **RF-Auth:** Solicitar credenciais quando ausentes e salvar de forma segura.
5. **RF-Play:** Reproduzir stream RTSP (H.264/H.265) com reconexão automática.
6. **RF-PTZ:** Expor controles PTZ (pan/tilt/zoom) quando o dispositivo suportar ONVIF PTZ.
7. **RF-Manage:** Permitir salvar, editar, renomear e excluir dispositivos.
8. **RF-Diagnostics:** Tela de teste de conectividade (OPTIONS/DESCRIBE RTSP, GetCapabilities ONVIF) com mensagens claras.

### 1.3. Requisitos Não Funcionais (RNF)

* **RNF-Sec:** Armazenar segredos via **EncryptedSharedPreferences** (Android Keystore).
* **RNF-UX:** Time-to-first-frame ≤ 2.5s em LAN típica; feedback de carregamento e erros com códigos e ação sugerida.
* **RNF-Perf:** Player deve suportar **H.264** e **H.265**; queda suave para sub‑stream quando main falhar.
* **RNF-Resil:** Reconectar em caso de troca de IP (DHCP) usando cache de descoberta + varredura rápida.
* **RNF-Log:** Logging estruturado (nivelado), com opção de exportar relatório de diagnóstico.

---

## 2. Arquitetura Lógica

**Camadas:**

* **UI/UX:** Telas (Scan QR, Seleção de Dispositivo, Player, PTZ, Diagnóstico, Configurações).
* **Domain:** Casos de uso (Scan → Normalize → Discover → Validate → Save → Play).
* **Data:** Repositórios de dispositivos, credenciais, cache de descoberta.
* **Infra:** ONVIF Client, WS‑Discovery, RTSP Player, Crypto/Storage, Networking.

**Fluxo principal:**

1. **Scan QR** → 2. **Parse/Detect** → 3. **Normalize** → 4. *(opcional)* **Discover** → 5. **Validate** → 6. **Persist** → 7. **Play** → 8. *(opcional)* **PTZ**.

---

## 3. Especificação de Dados

### 3.1. `CameraConnection` (modelo normalizado)

```json
{
  "proto": "rtsp",
  "ip": "192.168.1.45",
  "port": 554,
  "user": "admin",
  "pass": "***",
  "path": "/onvif1",
  "ptz": true,
  "onvif": { "deviceService": "http://192.168.1.45:80/onvif/device_service" },
  "uid": "optional",
  "meta": { "brand": "", "model": "", "source": "qr-rtsp|qr-json|qr-uid|manual" }
}
```

### 3.2. Entidades de Persistência

* **Device**: `id(uuid)`, `name`, `brand`, `model`, `uid?`, `lastKnownIp`, `rtspHost`, `rtspPort`, `rtspPath`, `hasPtz`, `onvifDeviceService?`, `createdAt`, `updatedAt`.
* **Credential**: `deviceId`, `username`, `password(enc)`.
* **StreamProfile** (opcional): `deviceId`, `token`, `streamUri`, `codec`, `resolution`, `bitrate`.
* **DiscoveryCache**: `uid?`, `mac?`, `candidateIps[]`, `ts`.

---

## 4. Formatos de QR suportados

### 4.1. RTSP URL

* Ex.: `rtsp://user:pass@192.168.1.45:554/onvif1`
* Ação: parse direto → `CameraConnection` completo.

### 4.2. JSON

```json
{
  "proto": "onvif|rtsp",
  "ip": "192.168.1.45",
  "port": 554,
  "user": "admin",
  "pass": "123456",
  "path": "/onvif1",
  "onvif_service": "http://192.168.1.45:80/onvif/device_service",
  "ptz": true,
  "uid": "G123-456-789"
}
```

* Ação: mapear campos; se faltar `path`, usar ONVIF para obter `GetStreamUri`.

### 4.3. UID/Serial proprietário (texto livre)

* Ex.: `G123-456-789-ABC`
* Ação: executar **WS‑Discovery** → ONVIF → verificar `SerialNumber`. Se houver match, montar `CameraConnection`.

---

## 5. Descoberta & ONVIF

### 5.1. WS‑Discovery

* Enviar `Probe` multicast (`urn:schemas-xmlsoap-org:ws:2005:04:discovery`).
* Agrupar respostas contendo tipos `NetworkVideoTransmitter`.

### 5.2. ONVIF (SOAP)

* **GetCapabilities(Device/Media/PTZ)** → detectar serviços e URI do Device Service.
* **GetDeviceInformation** → marca/modelo/serial (comparar com UID quando existir).
* **GetProfiles / GetStreamUri** → obter RTSP final para `main` e `sub`.
* **PTZ (se disponível)**: `GetConfigurations`, `ContinuousMove`, `RelativeMove`, `AbsoluteMove`, `Stop`, `GetStatus`.

### 5.3. Estratégia de seleção de stream

* Preferir `main` (H.264/H.265) e oferecer troca para `sub` via UI (menu Qualidade).

---

## 6. Player RTSP

### 6.1. Opção A (padrão): **ExoPlayer RTSP**

* Suporta H.264; H.265 pode requerer fallback.
* Reconexão automática com backoff exponencial.

### 6.2. Opção B (fallback): **libVLC**

* Melhor compat. com H.265 e peculiaridades de RTSP.
* Encapsular por interface de player para permitir swap.

### 6.3. Requisitos do player

* Latência baixa; controle de buffer ajustável.
* Modo tela cheia; snapshot; gravação local opcional.

---

## 7. Segurança

* **Credenciais**: armazenar via **EncryptedSharedPreferences** (Android Keystore).
* **RTSP URL**: **não** persistir a senha concatenada; montar em memória.
* **Permissões**: solicitar apenas necessárias (Câmera para QR, Rede, Armazenamento se gravar).
* **Logs**: nunca incluir senhas.

---

## 8. UX/Telas (resumo)

1. **Scan QR**

   * Estado: Scanning → Parsing → Normalizing.
2. **Encontrando sua câmera…**

   * WS‑Discovery com indicador de progresso; lista candidatos se >1.
3. **Credenciais**

   * Inputs username/password quando ausentes; botão “Testar conexão”.
4. **Player**

   * Exibe vídeo; ações: Mudo, Snapshot, Gravar, Qualidade (Main/Sub), PTZ (se suportado).
5. **Diagnóstico**

   * Botões: `OPTIONS`/`DESCRIBE` RTSP; `GetCapabilities`; exibir resultados e latência.
6. **Lista de Dispositivos**

   * Renomear, editar, excluir; status online/offline.

---

## 9. API Interna (Interfaces Kotlin)

```kotlin
sealed class QrPayload { data class Rtsp(val url: String): QrPayload(); data class Json(val json: String): QrPayload(); data class Uid(val uid: String): QrPayload() }

data class CameraConnection(
  var proto: String? = null,
  var ip: String? = null,
  var port: Int? = null,
  var user: String? = null,
  var pass: String? = null,
  var path: String? = null,
  var ptz: Boolean? = null,
  var onvifDeviceService: String? = null,
  var uid: String? = null,
  var meta: Map<String,String> = emptyMap()
)

interface QrParser { fun parse(text: String): QrPayload }
interface Normalizer { suspend fun normalize(payload: QrPayload): CameraConnection }
interface OnvifClient {
  suspend fun probe(timeoutMs: Long = 2500): List<OnvifEndpoint>
  suspend fun getCapabilities(endpoint: OnvifEndpoint, auth: Auth?): Capabilities
  suspend fun getDeviceInformation(endpoint: OnvifEndpoint, auth: Auth?): DeviceInfo
  suspend fun getProfiles(endpoint: OnvifEndpoint, auth: Auth?): List<MediaProfile>
  suspend fun getStreamUri(endpoint: OnvifEndpoint, profileToken: String, auth: Auth?): String
  suspend fun ptzContinuousMove(endpoint: OnvifEndpoint, vx: Float, vy: Float, vz: Float, auth: Auth?)
  suspend fun ptzStop(endpoint: OnvifEndpoint, auth: Auth?)
}

interface RtspProber { suspend fun options(uri: String, auth: Auth?): RtspOptions; suspend fun describe(uri: String, auth: Auth?): Sdp }
interface Player { fun play(uri: String, auth: Auth?); fun stop(); fun snapshot(): ByteArray? }
```

---

## 10. Fluxos de Caso de Uso (pseudocódigo)

### 10.1. Onboarding via QR

```kotlin
val payload = qrParser.parse(scannedText)
val conn = normalizer.normalize(payload) // pode disparar probe + onvif
val auth = credentialStore.getOrAsk(conn)
val uri = buildRtspUri(conn, auth)
validateRtsp(uri, auth) // OPTIONS/DESCRIBE com timeout
saveDevice(conn, auth)
navigateToPlayer(uri)
```

### 10.2. PTZ (gestos → comandos)

```kotlin
fun onPanTilt(deltaX: Float, deltaY: Float) {
  onvifClient.ptzContinuousMove(endpoint, vx = deltaX, vy = deltaY, vz = 0f, auth)
}
fun onZoom(delta: Float) {
  onvifClient.ptzContinuousMove(endpoint, vx = 0f, vy = 0f, vz = delta, auth)
}
fun onGestureEnd() { onvifClient.ptzStop(endpoint, auth) }
```

---

## 11. Estratégia de Reconexão & IP Dinâmico

* Ao falhar `DESCRIBE`/play, executar **varredura rápida** (Probe 800–1200ms) e testar candidatos no mesmo `uid`/`mac`.
* Atualizar `lastKnownIp` ao recuperar.

---

## 12. Tratamento de Erros (catálogo)

* **E-QR-UNSUPPORTED**: QR inválido/inesperado → mensagem com exemplos suportados.
* **E-DISC-NONE**: Nenhum dispositivo encontrado → sugerir verificar Wi‑Fi/2.4G e mesma sub-rede.
* **E-AUTH**: Credenciais incorretas → retry com dica de defaults conhecidos.
* **E-RTSP-DESCRIBE**: Stream indisponível → oferecer troca para sub-stream.
* **E-PTZ-UNAVAIL**: PTZ não suportado → esconder controles.

---

## 13. Telemetria (opcional)

* Eventos: `qr_scanned`, `normalize_done`, `probe_count`, `profiles_found`, `rtsp_describe_ms`, `first_frame_ms`, `reconnect_attempt`.
* Não registrar dados sensíveis (mascarar hosts/usuários).

---

## 14. Testes & Critérios de Aceite

### 14.1. Unit

* Parser de QR (RTSP/JSON/UID) com casos de borda.
* Normalizer sem/with ONVIF service; preenchimento de portas/caminhos default.
* Builder de RTSP (rtsps, user/pass, path leading slash).

### 14.2. Integração (em LAN de teste)

* WS‑Discovery encontra ≥1 câmera em ≤2.5s.
* GetProfiles retorna perfis; GetStreamUri válido.
* Player inicia em ≤2.5s (H.264 main); alterna para sub-stream.
* PTZ move/para conforme gestos; sem drift.

### 14.3. UX/E2E

* Onboarding completo a partir de três QRs: RTSP, JSON parcial, UID.
* Diagnóstico exibe razões claras para falhas comuns.

**DoD (Definition of Done):** Todos RFs implementados; testes acima verdes; relatório de diagnóstico exportável; checklist de segurança ok.

---

## 15. Ferramentas & Dependências

* **QR**: ZXing ou ML Kit Barcode.
* **ONVIF/WS‑Discovery**: cliente SOAP leve (ksoap2 ou implementação custom via OkHttp + XML), UDP multicast.
* **RTSP Player**: ExoPlayer (módulo RTSP) + fallback libVLC.
* **Storage Seguro**: EncryptedSharedPreferences.
* **DI/Arquitetura**: Kotlin, Coroutines/Flow, ViewModel, Repository.

---

## 16. Estrutura de Pastas (sugestão)

```
app/
  data/
    repo/
    storage/
    onvif/
    discovery/
  domain/
    model/
    usecase/
  ui/
    scan/
    onboarding/
    player/
    ptz/
    diagnostics/
  core/
    network/
    security/
    util/
```

---

## 17. Roadmap Incremental

1. **M1**: Scan QR → Normalizer (RTSP/JSON/UID) + persistência básica.
2. **M2**: WS‑Discovery + ONVIF (profiles/streamUri) + Diagnóstico.
3. **M3**: Player RTSP (main/sub) + reconexão + UX de qualidade.
4. **M4**: PTZ + Snap/Record local + export de logs.
5. **M5**: Hardening (segurança, testes, caching, varredura rápida IP).

---

## 18. Observações Finais

* Alguns modelos podem **não** expor ONVIF/RTSP por padrão; nesse caso, o fluxo depende de credencial/caminho específicos do fabricante. Manter lista de caminhos comuns (`/onvif1`, `/live/ch00_0`, etc.) como fallback.
* Para iOS, manter as mesmas interfaces e trocar implementações (MobileVLCKit, URLSession+SOAP, Keychain).
