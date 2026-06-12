package com.chukchukhaksa.mobile.common.analytics

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.core.events.Identify

class AndroidAnalyticsClient(
    context: Context,
    apiKey: String,
) : AnalyticsClient {

    private val amplitude = Amplitude(
        Configuration(
            apiKey = apiKey,
            context = context,
            autocapture = setOf(),
        ),
    )

    override fun setUserId(userId: String?) {
        amplitude.setUserId(userId)
    }

    override fun track(eventType: String, properties: Map<String, Any?>) {
        amplitude.track(eventType, properties.toMutableMap())
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        val identify = Identify()
        properties.forEach { (key, value) ->
            when (value) {
                is Int -> identify.set(key, value)
                is Long -> identify.set(key, value)
                is Double -> identify.set(key, value)
                is Boolean -> identify.set(key, value)
                is String -> identify.set(key, value)
                null -> {}
                else -> identify.set(key, value.toString())
            }
        }
        amplitude.identify(identify)
    }
}
