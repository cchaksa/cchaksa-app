package com.chukchukhaksa.mobile.common.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlin.math.sqrt

// 가속도가 중력의 2.7배를 넘으면 흔든 것으로 본다. 한 번 흔들 때 여러 번 넘으므로 1초 동안은 다시 부르지 않는다.
private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
private const val SHAKE_COOLDOWN_MILLIS = 1_000L

@Composable
actual fun ShakeEffect(onShake: () -> Unit) {
  val context = LocalContext.current
  val currentOnShake by rememberUpdatedState(onShake)

  // 앱이 백그라운드에 있는 동안에는 센서를 끈다.
  LifecycleResumeEffect(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val listener = object : SensorEventListener {
      private var lastShakeAt = 0L

      override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = event.values
        val gravity = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        val now = SystemClock.elapsedRealtime()
        if (gravity > SHAKE_THRESHOLD_GRAVITY && now - lastShakeAt > SHAKE_COOLDOWN_MILLIS) {
          lastShakeAt = now
          currentOnShake()
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }
    if (accelerometer != null) {
      sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }
    onPauseOrDispose { sensorManager?.unregisterListener(listener) }
  }
}
