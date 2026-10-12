package com.chukchukhaksa.mobile.domain.config.usecase

import com.chukchukhaksa.mobile.common.kmp.Platform
import com.chukchukhaksa.mobile.domain.config.repository.AppConfigRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckNeedForceUpdateUseCaseTest {

    private class MockAppConfigRepository : AppConfigRepository {
        var androidMinVersion: String = "1.0.0"
        var iosMinVersion: String = "1.0.0"
        var appleStoreUrl: String = "https://apps.apple.com/app/id0000000000"
        var googleStoreUrl: String = "https://play.google.com/store/apps/details?id=com.kunize.uswtimetable"

        override suspend fun getAndroidMinVersion(): String = androidMinVersion
        override suspend fun getIOSMinVersion(): String = iosMinVersion
        override suspend fun getAppleStoreUrl(): String = appleStoreUrl
        override suspend fun getGoogleStoreUrl(): String = googleStoreUrl
    }

    private val mockRepository = MockAppConfigRepository()
    private val useCase = CheckNeedForceUpdateUseCase(mockRepository)

    @Test
    fun shouldReturnTrueWhenCurrentVersionIsLowerThanMinimumVersionForAndroid() = runTest {
        mockRepository.androidMinVersion = "2.0.0"

        val result = useCase(Platform.Android, "1.5.0").getOrThrow()

        assertTrue(result.needForceUpdate)
        assertEquals(mockRepository.googleStoreUrl, result.storeUrl)
    }

    @Test
    fun shouldReturnFalseWhenCurrentVersionIsHigherThanMinimumVersionForAndroid() = runTest {
        mockRepository.androidMinVersion = "1.0.0"

        val result = useCase(Platform.Android, "2.0.0").getOrThrow()

        assertFalse(result.needForceUpdate)
        assertNull(result.storeUrl)
    }

    @Test
    fun shouldReturnFalseWhenCurrentVersionEqualsMinimumVersionForAndroid() = runTest {
        mockRepository.androidMinVersion = "1.5.0"

        val result = useCase(Platform.Android, "1.5.0").getOrThrow()

        assertFalse(result.needForceUpdate)
    }

    @Test
    fun shouldHandleIOSPlatformCorrectly() = runTest {
        mockRepository.iosMinVersion = "2.1.0"

        val result = useCase(Platform.IOS, "2.0.0").getOrThrow()

        assertTrue(result.needForceUpdate)
        assertEquals(mockRepository.appleStoreUrl, result.storeUrl)
    }

    @Test
    fun shouldHandlePatchVersionComparisonCorrectly() = runTest {
        mockRepository.androidMinVersion = "1.0.1"

        val result = useCase(Platform.Android, "1.0.0").getOrThrow()

        assertTrue(result.needForceUpdate)
        assertEquals(mockRepository.googleStoreUrl, result.storeUrl)
    }

    @Test
    fun shouldHandleDifferentVersionLengthFormats() = runTest {
        mockRepository.androidMinVersion = "1.0.0.1"

        val result = useCase(Platform.Android, "1.0.0").getOrThrow()

        assertTrue(result.needForceUpdate)
        assertEquals(mockRepository.googleStoreUrl, result.storeUrl)
    }

    @Test
    fun shouldHandleVersionWithMissingParts() = runTest {
        mockRepository.androidMinVersion = "1.0"

        val result = useCase(Platform.Android, "1.0.0").getOrThrow()

        assertFalse(result.needForceUpdate)
    }

    @Test
    fun shouldHandleInvalidVersionNumbersGracefully() = runTest {
        mockRepository.androidMinVersion = "1.a.0"

        val result = useCase(Platform.Android, "1.0.0")

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldHandleMajorVersionDifference() = runTest {
        mockRepository.androidMinVersion = "2.0.0"

        val result = useCase(Platform.Android, "1.9.9").getOrThrow()

        assertTrue(result.needForceUpdate)
        assertEquals(mockRepository.googleStoreUrl, result.storeUrl)
    }

    @Test
    fun shouldHandleMinorVersionDifference() = runTest {
        mockRepository.androidMinVersion = "1.5.0"

        val result = useCase(Platform.Android, "1.4.9").getOrThrow()

        assertTrue(result.needForceUpdate)
        assertEquals(mockRepository.googleStoreUrl, result.storeUrl)
    }

    @Test
    fun shouldThrowExceptionForInvalidCurrentVersion() = runTest {
        mockRepository.androidMinVersion = "1.0.0"

        val result = useCase(Platform.Android, "1.a.0")

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldThrowExceptionForEmptyVersionString() = runTest {
        mockRepository.androidMinVersion = "1.0.0"

        val result = useCase(Platform.Android, "")

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldThrowExceptionForMinVersionAsEmptyString() = runTest {
        mockRepository.androidMinVersion = ""

        val result = useCase(Platform.Android, "1.0.0")

        assertTrue(result.isFailure)
    }
}
