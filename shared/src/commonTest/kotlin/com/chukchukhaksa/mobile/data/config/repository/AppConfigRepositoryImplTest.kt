package com.chukchukhaksa.mobile.data.config.repository

import com.chukchukhaksa.mobile.data.config.datasource.RemoteAppConfigDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppConfigRepositoryImplTest {

    private class MockRemoteAppConfigDataSource : RemoteAppConfigDataSource {
        var minVersion: String = "1.0.0"
        var storeUrl: String = "https://play.google.com/store/apps/details?id=com.kunize.uswtimetable"

        override suspend fun getMinVersion(): String = minVersion
        override suspend fun getStoreUrl(): String = storeUrl
    }

    private val mockDataSource = MockRemoteAppConfigDataSource()
    private val repository = AppConfigRepositoryImpl(mockDataSource)

    @Test
    fun shouldReturnTrueWhenCurrentVersionIsLowerThanMinimumVersion() = runTest {
        mockDataSource.minVersion = "2.0.0"

        val result = repository.getForceUpdateInfo("1.5.0")

        assertTrue(result.needForceUpdate)
        assertEquals(mockDataSource.storeUrl, result.storeUrl)
    }

    @Test
    fun shouldReturnFalseWhenCurrentVersionIsHigherThanMinimumVersion() = runTest {
        mockDataSource.minVersion = "1.0.0"

        val result = repository.getForceUpdateInfo("2.0.0")

        assertFalse(result.needForceUpdate)
        assertNull(result.storeUrl)
    }

    @Test
    fun shouldReturnFalseWhenCurrentVersionEqualsMinimumVersion() = runTest {
        mockDataSource.minVersion = "1.5.0"

        val result = repository.getForceUpdateInfo("1.5.0")

        assertFalse(result.needForceUpdate)
    }

    @Test
    fun shouldHandlePatchVersionComparisonCorrectly() = runTest {
        mockDataSource.minVersion = "1.0.1"

        val result = repository.getForceUpdateInfo("1.0.0")

        assertTrue(result.needForceUpdate)
        assertEquals(mockDataSource.storeUrl, result.storeUrl)
    }

    @Test
    fun shouldHandleDifferentVersionLengthFormats() = runTest {
        mockDataSource.minVersion = "1.0.0.1"

        val result = repository.getForceUpdateInfo("1.0.0")

        assertTrue(result.needForceUpdate)
        assertEquals(mockDataSource.storeUrl, result.storeUrl)
    }

    @Test
    fun shouldHandleVersionWithMissingParts() = runTest {
        mockDataSource.minVersion = "1.0"

        val result = repository.getForceUpdateInfo("1.0.0")

        assertFalse(result.needForceUpdate)
    }

    @Test
    fun shouldHandleInvalidVersionNumbersGracefully() = runTest {
        mockDataSource.minVersion = "1.a.0"

        val result = runCatching { repository.getForceUpdateInfo("1.0.0") }

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldHandleMajorVersionDifference() = runTest {
        mockDataSource.minVersion = "2.0.0"

        val result = repository.getForceUpdateInfo("1.9.9")

        assertTrue(result.needForceUpdate)
        assertEquals(mockDataSource.storeUrl, result.storeUrl)
    }

    @Test
    fun shouldHandleMinorVersionDifference() = runTest {
        mockDataSource.minVersion = "1.5.0"

        val result = repository.getForceUpdateInfo("1.4.9")

        assertTrue(result.needForceUpdate)
        assertEquals(mockDataSource.storeUrl, result.storeUrl)
    }

    @Test
    fun shouldThrowExceptionForInvalidCurrentVersion() = runTest {
        mockDataSource.minVersion = "1.0.0"

        val result = runCatching { repository.getForceUpdateInfo("1.a.0") }

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldThrowExceptionForEmptyVersionString() = runTest {
        mockDataSource.minVersion = "1.0.0"

        val result = runCatching { repository.getForceUpdateInfo("") }

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldThrowExceptionForMinVersionAsEmptyString() = runTest {
        mockDataSource.minVersion = ""

        val result = runCatching { repository.getForceUpdateInfo("1.0.0") }

        assertTrue(result.isFailure)
    }
}
