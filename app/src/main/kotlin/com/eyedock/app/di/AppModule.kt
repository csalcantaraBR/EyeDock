package com.eyedock.app.di

import android.content.Context
import com.eyedock.app.data.normalizer.NormalizerImpl
import com.eyedock.app.data.onvif.OnvifClientImpl
import com.eyedock.app.data.player.ExoPlayerImpl
import com.eyedock.app.data.qr.QrParserImpl
import com.eyedock.app.data.repository.DeviceRepository
import com.eyedock.app.data.rtsp.RtspProberImpl
import com.eyedock.app.data.security.SecureCredentialStore
import com.eyedock.app.domain.interfaces.*
import com.eyedock.app.domain.usecase.OnboardingUseCase
import com.eyedock.app.domain.usecase.PtzUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideQrParser(): QrParser {
        return QrParserImpl()
    }

    @Provides
    @Singleton
    fun provideOnvifClient(httpClient: OkHttpClient): OnvifClient {
        return OnvifClientImpl(httpClient)
    }

    @Provides
    @Singleton
    fun provideNormalizer(onvifClient: OnvifClient): Normalizer {
        return NormalizerImpl(onvifClient)
    }

    @Provides
    @Singleton
    fun provideRtspProber(httpClient: OkHttpClient): RtspProber {
        return RtspProberImpl(httpClient)
    }

    @Provides
    @Singleton
    fun providePlayer(@ApplicationContext context: Context): Player {
        return ExoPlayerImpl(context)
    }

    @Provides
    @Singleton
    fun provideSecureCredentialStore(@ApplicationContext context: Context): SecureCredentialStore {
        return SecureCredentialStore(context)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(): DeviceRepository {
        return DeviceRepository()
    }

    @Provides
    @Singleton
    fun provideOnboardingUseCase(
        qrParser: QrParser,
        normalizer: Normalizer,
        rtspProber: RtspProber,
        deviceRepository: DeviceRepository,
        credentialStore: SecureCredentialStore
    ): OnboardingUseCase {
        return OnboardingUseCase(qrParser, normalizer, rtspProber, deviceRepository, credentialStore)
    }

    @Provides
    @Singleton
    fun providePtzUseCase(
        onvifClient: OnvifClient,
        deviceRepository: DeviceRepository,
        credentialStore: SecureCredentialStore
    ): PtzUseCase {
        return PtzUseCase(onvifClient, deviceRepository, credentialStore)
    }
}
