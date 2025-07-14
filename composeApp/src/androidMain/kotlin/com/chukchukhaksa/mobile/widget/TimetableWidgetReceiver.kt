package com.chukchukhaksa.mobile.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.chukchukhaksa.mobile.common.model.Timetable
import com.chukchukhaksa.mobile.common.ui.encodeToUri
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform.getKoin

class TimetableWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = TimetableWidget()

  private fun observeData(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
      val getMainTimetableUseCase: GetMainTimetableUseCase = getKoin().get()

      val timetable = getMainTimetableUseCase().getOrNull() ?: Timetable()
      Napier.i("TimetableWidgetReceiver: observeData: timetable = $timetable")

      val timetableUri = Json.encodeToUri(timetable)
      val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(TimetableWidget::class.java)

      glanceIds.forEach { glanceId ->
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
          prefs.toMutablePreferences().apply {
            this[timetableWidgetKey] = timetableUri
          }
        }
      }

      glanceAppWidget.updateAll(context)
    }
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    observeData(context)
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  override fun onReceive(context: Context, intent: Intent) {
    observeData(context)
    super.onReceive(context, intent)
  }

  companion object {
    val timetableWidgetKey = stringPreferencesKey("[key] is timetable widget")
  }
}
