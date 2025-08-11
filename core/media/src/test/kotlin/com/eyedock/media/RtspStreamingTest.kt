package com.eyedock.media

import com.eyedock.core.common.test.categories.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Testes de regressão para streaming RTSP
 * Foca em estabilidade, performance e cenários edge cases
 */
@DisplayName("RTSP Streaming Regression Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RtspStreamingTest {

    @Mock
    private lateinit var mockRtspClient: RtspClient
    
    private lateinit var rtspClient: RtspClient
    private lateinit var streamManager: StreamManager

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        rtspClient = RtspClient()
        streamManager = StreamManager(rtspClient)
    }

    @Nested
    @DisplayName("Basic Connection Tests")
    class BasicConnectionTests {

        @Test
        @Tag(REGRESSION_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Deve validar URL RTSP válida")
        fun `deve validar URL RTSP valida`() = runBlocking {
            // Arrange
            val validUrl = "rtsp://192.168.1.100:554/onvif1"
            
            // Act & Assert
            assertTrue(rtspClient.isValidRtspUrl(validUrl), "URL RTSP válida deve ser aceita")
        }

        @Test
        @Tag(REGRESSION_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Deve validar URL RTSP inválida")
        fun `deve validar URL RTSP invalida`() = runBlocking {
            // Arrange
            val invalidUrl = "http://invalid.com/stream"
            
            // Act & Assert
            assertFalse(rtspClient.isValidRtspUrl(invalidUrl), "URL não-RTSP deve ser rejeitada")
        }

        @Test
        @Tag(REGRESSION_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Deve conectar a stream RTSP básico")
        fun `deve conectar a stream RTSP basico`() = runBlocking {
            // Arrange
            val testUrl = "rtsp://192.168.1.100:554/onvif1"
            
            // Act
            val result = rtspClient.connect(testUrl)
            
            // Assert
            assertTrue(result.isSuccess, "Conexão deve ser bem-sucedida")
        }
    }

    @Nested
    @DisplayName("Stream Manager Tests")
    class StreamManagerTests {

        @Test
        @Tag(REGRESSION_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Deve gerenciar múltiplas conexões")
        fun `deve gerenciar multiplas conexoes`() = runBlocking {
            // Arrange
            val stream1 = "rtsp://192.168.1.100:554/onvif1"
            val stream2 = "rtsp://192.168.1.101:554/onvif1"
            
            // Act
            val result1 = streamManager.addStream(stream1)
            val result2 = streamManager.addStream(stream2)
            
            // Assert
            assertTrue(result1, "Primeira stream deve ser adicionada")
            assertTrue(result2, "Segunda stream deve ser adicionada")
            assertEquals(2, streamManager.getActiveStreamCount(), "Deve ter 2 streams ativas")
        }

        @Test
        @Tag(REGRESSION_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Deve remover stream corretamente")
        fun `deve remover stream corretamente`() = runBlocking {
            // Arrange
            val stream = "rtsp://192.168.1.100:554/onvif1"
            streamManager.addStream(stream)
            
            // Act
            val result = streamManager.removeStream(stream)
            
            // Assert
            assertTrue(result, "Stream deve ser removida com sucesso")
            assertEquals(0, streamManager.getActiveStreamCount(), "Deve ter 0 streams ativas")
        }
    }

    @Nested
    @DisplayName("Stream Analyzer Tests")
    class StreamAnalyzerTests {

        @Test
        @Tag(REGRESSION_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Deve analisar stream RTSP")
        fun `deve analisar stream RTSP`() = runBlocking {
            // Arrange
            val streamAnalyzer = StreamAnalyzer()
            val rtspUrl = "rtsp://192.168.1.100:554/onvif1"
            
            // Act
            val streamInfo = streamAnalyzer.analyzeStream(rtspUrl)
            
            // Assert
            assertNotNull(streamInfo, "Análise deve retornar informações da stream")
            assertTrue(streamInfo.isValid, "Stream deve ser válida")
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @Tag(PERFORMANCE_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Conexão deve ser estabelecida rapidamente")
        fun `conexao deve ser estabelecida rapidamente`() = runBlocking {
            // Arrange
            val streamUrl = "rtsp://192.168.1.100:554/onvif1"
            val maxConnectionTimeMs = 5000L // 5 segundos
            
            // Act
            val startTime = System.currentTimeMillis()
            val result = rtspClient.connect(streamUrl)
            val connectionTime = System.currentTimeMillis() - startTime
            
            // Assert
            assertTrue(connectionTime <= maxConnectionTimeMs, 
                "Conexão deve ser estabelecida em ≤ ${maxConnectionTimeMs}ms, atual: ${connectionTime}ms")
        }

        @Test
        @Tag(PERFORMANCE_TEST)
        @Tag(MEDIA_TEST)
        @DisplayName("Múltiplas conexões simultâneas")
        fun `multiplas conexoes simultaneas`() = runBlocking {
            // Arrange
            val streamUrls = listOf(
                "rtsp://192.168.1.100:554/onvif1",
                "rtsp://192.168.1.101:554/onvif1",
                "rtsp://192.168.1.102:554/onvif1"
            )
            
            // Act
            val results = streamUrls.map { url ->
                rtspClient.connect(url)
            }
            
            // Assert
            val successfulConnections = results.count { it.isSuccess }
            assertTrue(successfulConnections >= 2, 
                "Pelo menos 2 de 3 conexões devem ser bem-sucedidas")
        }
    }
}
