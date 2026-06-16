package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppPreferences
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.DeepSpaceStarryBackground

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val appPreferences = AppPreferences.getInstance(applicationContext)
    
    setContent {
      val isDarkMode by appPreferences.isDarkMode.collectAsStateWithLifecycle()
      val isMidnightMode by appPreferences.isMidnightMode.collectAsStateWithLifecycle()
      
      MyApplicationTheme(darkTheme = isDarkMode, isMidnightTheme = isMidnightMode) {
        DeepSpaceStarryBackground(
          modifier = Modifier.fillMaxSize(),
          isDarkMode = isDarkMode,
          isMidnightMode = isMidnightMode
        ) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDarkMode) Color.Transparent else MaterialTheme.colorScheme.background
          ) {
            AppNavigation()
          }
        }
      }
    }
  }
}
