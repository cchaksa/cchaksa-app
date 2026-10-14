package com.chukchukhaksa.mobile.data.config.datasource

/** 현재 플랫폼의 앱 설정 값을 읽는다. 플랫폼별 키 선택은 구현체가 맡는다. */
interface RemoteAppConfigDataSource {
    suspend fun getMinVersion(): String
    suspend fun getStoreUrl(): String
}
