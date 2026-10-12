package com.chukchukhaksa.mobile.widget

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.chukchukhaksa.mobile.common.model.Timetable
import kotlinx.cinterop.ExperimentalForeignApi
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase
import org.koin.mp.KoinPlatform.getKoin
import kotlinx.coroutines.runBlocking
import io.github.aakira.napier.Napier

// JSON configuration that includes default values (empty lists)
private val widgetJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@OptIn(ExperimentalForeignApi::class)
actual fun sendWidgetUpdateCommand(context: Any) {
    try {
        val getMainTimetableUseCase: GetMainTimetableUseCase = getKoin().get()
        val timetable = runBlocking {
            getMainTimetableUseCase().getOrNull()
        }

        if (timetable != null) {
            val sharedDefaults = NSUserDefaults(suiteName = "group.com.kunize.uswtimetable.shared")
            val timetableJson = widgetJson.encodeToString(timetable)

            sharedDefaults.setObject(timetableJson, forKey = "timetable_data")
            sharedDefaults.synchronize()

            NSNotificationCenter.defaultCenter.postNotificationName(
                "TimetableDataUpdated",
                `object` = null
            )
        }
    } catch (e: Exception) {
        Napier.e(tag = "WidgetUpdate", message = "Failed to update widget: ${e.message}", throwable = e)
    }
}
