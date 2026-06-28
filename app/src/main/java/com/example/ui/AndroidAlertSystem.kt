package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.Task

object AndroidAlertSystem {
    private const val CHANNEL_ID = "critical_risk_channel"
    private const val CHANNEL_NAME = "Critical Risk Alerts"
    private const val CHANNEL_DESC = "Notifications for high deadline risk tasks needing emergency action"

    /**
     * Triggers a system-level high-priority notification and plays an aggressive haptic
     * distress vibration pattern on the device.
     */
    fun triggerCriticalAlert(context: Context, task: Task) {
        // 1. Setup Notification Channel for API 26+
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create PendingIntent to launch the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Build and Dispatch Native System Notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning) // default system warning icon
            .setContentTitle("🚨 CRITICAL DEADLINE RISK")
            .setContentText("${task.title} has a ${task.completionProbability}% chance of completion. Emergency action required!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "\"${task.title}\" has been flagged as HIGH RISK by Predictive Risk Analyzer.\n" +
                "Estimated completion chance is critical at ${task.completionProbability}%. " +
                "Activate Emergency Rescue Overdrive to secure your schedules."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(task.id.toInt(), notification)

        // 3. Trigger Aggressive Device Vibration (Haptic Distress Pattern)
        triggerHapticVibration(context)
    }

    /**
     * Triggers a distinct, pulsating physical distress pattern using Android's Vibrator API.
     * Pattern: Vibrate 500ms, Pause 200ms, Vibrate 500ms, Pause 200ms, Vibrate 1000ms.
     */
    fun triggerHapticVibration(context: Context) {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 1000)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (VibratorManager)
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                // Legacy Vibrator support
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(pattern, -1)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Triggers a system-level general alert notification (e.g., Pomodoro completed)
     * and a pleasant haptic confirmation pulse on the device.
     */
    fun triggerGeneralAlert(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "general_alerts_channel",
                "General Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for focus sessions, breaks, and general status events"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "general_alerts_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(999, notification)

        // Trigger light haptic pulse
        try {
            val pattern = longArrayOf(0, 100, 100, 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(pattern, -1)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
