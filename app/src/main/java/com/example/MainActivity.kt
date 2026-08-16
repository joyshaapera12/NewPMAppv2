package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.core.notifications.PitchMetricsNotificationManager
import com.example.core.storage.UserPreferencesRepository
import com.example.core.sync.MatchSyncWorker
import com.example.navigation.PitchMetricsApp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.PitchMetricsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PitchMetricsNotificationManager.initChannels(this)
        MatchSyncWorker.schedulePeriodicSync(this)
        enableEdgeToEdge()
        setContent {
            val userPrefs = remember { UserPreferencesRepository(applicationContext) }
            val themeMode by userPrefs.themeModeFlow.collectAsState(initial = AppThemeMode.DARK)

            PitchMetricsTheme(themeMode = themeMode) {
                PitchMetricsApp()
            }
        }
    }
}
