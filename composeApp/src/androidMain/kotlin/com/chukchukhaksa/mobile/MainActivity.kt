package com.chukchukhaksa.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chukchukhaksa.mobile.common.provider.LocalAppContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
          val context = LocalContext.current
          CompositionLocalProvider(LocalAppContext provides context) {
            App()
          }
        }
    }
}

@Preview()
@Composable
fun AppAndroidPreview() {
  App()
}
