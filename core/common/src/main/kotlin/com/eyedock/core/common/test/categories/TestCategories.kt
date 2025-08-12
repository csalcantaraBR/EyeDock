/**
 * Categorias de teste para organização e execução seletiva
 * Usado com JUnit 5 @Tag annotation
 */

package com.eyedock.core.common.test.categories

/**
 * Testes unitários rápidos (< 1s cada)
 * Executados frequentemente durante desenvolvimento
 */
const val FAST_TEST = "fast"

/**
 * Testes de integração (1-10s cada)
 * Testam interação entre componentes
 */
const val INTEGRATION_TEST = "integration"

/**
 * Testes de smoke - subset mínimo para validação rápida
 * Executados antes de commit
 */
const val SMOKE_TEST = "smoke"

/**
 * Testes que requerem dispositivo físico
 * Não podem ser executados em emulador
 */
const val DEVICE_ONLY_TEST = "device"

/**
 * Testes que simulam condições de rede
 * Podem ser lentos devido a timeouts
 */
const val NETWORK_TEST = "network"

/**
 * Testes específicos para funcionalidades ONVIF
 */
const val ONVIF_TEST = "onvif"

/**
 * Testes relacionados à descoberta de câmeras
 */
const val DISCOVERY_TEST = "discovery"

/**
 * Testes de streaming e reprodução de mídia
 */
const val MEDIA_TEST = "media"

/**
 * Testes relacionados ao Storage Access Framework
 */
const val STORAGE_TEST = "storage"

/**
 * Testes de PTZ (Pan-Tilt-Zoom)
 */
const val PTZ_TEST = "ptz"

/**
 * Testes de eventos (movimento, som)
 */
const val EVENTS_TEST = "events"

/**
 * Testes de áudio bidirecional
 */
const val AUDIO_TEST = "audio"

/**
 * Testes de UI e acessibilidade
 */
const val UI_TEST = "ui"

/**
 * Testes de acessibilidade (TalkBack, etc.)
 */
const val ACCESSIBILITY_TEST = "accessibility"

/**
 * Testes de performance e benchmark
 */
const val PERFORMANCE_TEST = "performance"

/**
 * Testes de segurança e privacidade
 */
const val SECURITY_TEST = "security"

/**
 * Testes end-to-end completos
 */
const val E2E_TEST = "e2e"

/**
 * Testes que requerem câmera real conectada
 * Usados para validação final
 */
const val REAL_CAMERA_TEST = "real_camera"

/**
 * Testes de regressão para builds de release
 */
const val REGRESSION_TEST = "regression"
