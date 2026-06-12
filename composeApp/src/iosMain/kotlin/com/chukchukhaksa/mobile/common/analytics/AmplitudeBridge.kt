package com.chukchukhaksa.mobile.common.analytics

interface AmplitudeBridge {
    fun setUserId(userId: String?)
    fun track(eventType: String, properties: Map<String, Any?>)
    fun setUserProperties(properties: Map<String, Any?>)
}
