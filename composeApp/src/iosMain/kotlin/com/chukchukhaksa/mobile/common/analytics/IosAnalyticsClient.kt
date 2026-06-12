package com.chukchukhaksa.mobile.common.analytics

class IosAnalyticsClient(
    private val bridge: AmplitudeBridge,
) : AnalyticsClient {

    override fun setUserId(userId: String?) {
        bridge.setUserId(userId)
    }

    override fun track(eventType: String, properties: Map<String, Any?>) {
        bridge.track(eventType, properties)
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        bridge.setUserProperties(properties)
    }
}
