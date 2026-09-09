package com.afomsteam.enlistedplanner

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.afomsteam.enlistedplanner.notifications.NotificationScheduler
import com.afomsteam.enlistedplanner.ui.EnlistedPlannerTheme
import com.afomsteam.enlistedplanner.ui.PlannerApp

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        NotificationScheduler.schedule(this)
        setContent { EnlistedPlannerTheme { PlannerApp() } }
    }
}
