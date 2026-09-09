package com.afomsteam.enlistedplanner.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.afomsteam.enlistedplanner.R
import com.afomsteam.enlistedplanner.data.PlannerRepository
import com.afomsteam.enlistedplanner.data.Severity
import com.afomsteam.enlistedplanner.logic.EnlistedBrain
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun schedule(context: Context) {
        val repo = PlannerRepository(context)
        if (!repo.load().settings.dailyNotifications) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            return
        }
        val hour = repo.load().settings.notificationHour.coerceIn(0,23)
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMinutes = Duration.between(now, next).toMinutes().coerceAtLeast(1)
        val request = PeriodicWorkRequestBuilder<AttentionWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
    private const val WORK_NAME = "enlisted-brain-daily-attention"
}

class AttentionWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val state = PlannerRepository(applicationContext).load()
        if (!state.settings.dailyNotifications) return Result.success()
        val actionable = EnlistedBrain.signals(state).filter { it.severity == Severity.CRITICAL || it.severity == Severity.WARNING }
        if (actionable.isEmpty()) return Result.success()

        val channelId = "enlisted_brain_attention"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Enlisted Brain Attention", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Local reminders for due career, troop, readiness and program actions."
            })
        }

        val first = actionable.first()
        val text = if (actionable.size == 1) first.title else "${actionable.size} items need attention • ${first.title}"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Enlisted Planner")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(actionable.take(4).joinToString("\n") { "• ${it.title}" }))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching { NotificationManagerCompat.from(applicationContext).notify(2001, notification) }
        return Result.success()
    }
}
