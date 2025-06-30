package com.chukchukhaksa.mobile.widget

import android.content.Context
import androidx.compose.material3.Text
import androidx.glance.appwidget.GlanceAppWidget
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.provideContent


class ChukChukWidget : GlanceAppWidget() {
  @Composable
  fun Content() {
    GlanceTheme {
      Text("Chuk Chuk Widget")
    }
  }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      Content()
    }
  }
}
