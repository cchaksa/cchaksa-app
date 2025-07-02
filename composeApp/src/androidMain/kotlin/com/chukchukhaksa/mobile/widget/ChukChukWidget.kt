package com.chukchukhaksa.mobile.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import com.chukchukhaksa.mobile.domain.timetable.usecase.GetMainTimetableUseCase
import io.github.aakira.napier.Napier
import org.koin.core.context.GlobalContext


class ChukChukWidget() : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    var loadTimetableResult = false
    val getMainTimetableUseCase: GetMainTimetableUseCase = GlobalContext.get().get()
    getMainTimetableUseCase()
      .onSuccess {
        loadTimetableResult = true
        Napier.e("Load timetable success !!!")
      }
      .onFailure {
        loadTimetableResult = false
        Napier.e("Load timetable fail !!!")
      }

    provideContent {
      GlanceTheme {
        if (loadTimetableResult) {
          Text("Chuk Chuk Widget")
        } else {
          ChukChukWidgetEmptyScreen()
        }
      }
    }
  }
}
