package com.chukchukhaksa.mobile.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import com.chukchukhaksa.mobile.common.model.Timetable
import kotlinx.serialization.json.Json
import com.chukchukhaksa.mobile.widget.timetable.GlanceTimetable
import com.chukchukhaksa.mobile.common.ui.decodeFromUri

class TimetableWidget : GlanceAppWidget() {

  override val sizeMode = SizeMode.Exact

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      val size = LocalSize.current
      val prefs = currentState<Preferences>()
      val timetable = try {
        val timetableUri = prefs[TimetableWidgetReceiver.timetableWidgetKey] ?: ""  // 이거 빈값 들어가면서 null 반환하고 이 때문에 위젯에 시간표 표출 불가능
        if (timetableUri.isBlank()) {
          null
        } else {
          Json.decodeFromUri<Timetable>(timetableUri)
        }
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
      GlanceTimetable(
        modifier = GlanceModifier
          .fillMaxSize(),
        size = size.width,
        timetable = timetable ?: Timetable() // null 방지
      )
    }
  }
}
