package com.chukchukhaksa.mobile.common.analytics

interface AnalyticsClient {
    fun setUserId(userId: String?)
    fun track(eventType: String, properties: Map<String, Any?> = emptyMap())
    fun setUserProperties(properties: Map<String, Any?>)
}
