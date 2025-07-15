package com.chukchukhaksa.mobile.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

actual fun sendWidgetUpdateCommand(context: Any) {
  if(context is Context) {
    context.sendBroadcast(
      Intent(
        context,
        TimetableWidgetReceiver::class.java,
      ).setAction(
        AppWidgetManager.ACTION_APPWIDGET_UPDATE,
      ),
    )
  }
}
