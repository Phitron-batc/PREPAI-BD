package com.example.data.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType

object NotificationEngine {

    const val CHANNEL_STUDY_ID = "prepai_study_reminders"
    const val CHANNEL_CIRCULARS_ID = "prepai_circular_alerts"
    const val CHANNEL_EXAMS_ID = "prepai_exam_alerts"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val studyChannel = NotificationChannel(
                CHANNEL_STUDY_ID,
                "Study & Revision Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily study plan tasks, spaced repetition revision reminders, and streak alerts."
            }

            val circularChannel = NotificationChannel(
                CHANNEL_CIRCULARS_ID,
                "Job Circular Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New verified government job circulars and deadline warnings."
            }

            val examChannel = NotificationChannel(
                CHANNEL_EXAMS_ID,
                "Mock Exam & Weakness Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled mock exam notices and targeted weakness remediation drills."
            }

            notificationManager.createNotificationChannel(studyChannel)
            notificationManager.createNotificationChannel(circularChannel)
            notificationManager.createNotificationChannel(examChannel)
        }
    }

    /**
     * Generates standard scheduled system notifications for candidate's daily workflow.
     */
    fun getInitialNotifications(): List<AppNotification> {
        val now = System.currentTimeMillis()
        return listOf(
            AppNotification(
                id = "notif_1",
                title = "47th BCS Preliminary Circular Published",
                message = "Bangladesh Public Service Commission has published 47th BCS notice for 3,460 posts. Check eligibility now!",
                type = NotificationType.CIRCULAR_ALERT,
                timestamp = now - 3600000L * 2,
                isRead = false,
                actionUrl = "circular_bcs_47"
            ),
            AppNotification(
                id = "notif_2",
                title = "Spaced Repetition: 8 Questions Due Today",
                message = "Review your bookmarked geometry formulas and English prepositions to retain long-term memory.",
                type = NotificationType.REVISION_REMINDER,
                timestamp = now - 3600000L * 5,
                isRead = false,
                actionUrl = "spaced_repetition"
            ),
            AppNotification(
                id = "notif_3",
                title = "Daily Study Target: 3 Tasks Pending",
                message = "Keep your 12-day streak alive! Complete today's Bangla Literature and ICT practice tasks.",
                type = NotificationType.DAILY_STUDY_REMINDER,
                timestamp = now - 3600000L * 10,
                isRead = true,
                actionUrl = "study_planner"
            ),
            AppNotification(
                id = "notif_4",
                title = "Weakness Alert: Mathematics Accuracy Below 50%",
                message = "Your Profit/Loss accuracy is 42%. Tap 'Fix My Weakness' to launch a 10-question recovery quiz.",
                type = NotificationType.WEAKNESS_ALERT,
                timestamp = now - 3600000L * 24,
                isRead = true,
                actionUrl = "weakness_detector"
            ),
            AppNotification(
                id = "notif_5",
                title = "Combined 9 Banks Senior Officer Exam Date Set",
                message = "The Bankers Selection Committee has scheduled the preliminary exam for next month.",
                type = NotificationType.EXAM_REMINDER,
                timestamp = now - 3600000L * 48,
                isRead = true,
                actionUrl = "mock_exams"
            )
        )
    }

    /**
     * Dispatches a local system notification if permissions are granted.
     */
    fun showLocalNotification(
        context: Context,
        notification: AppNotification
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val channelId = when (notification.type) {
            NotificationType.CIRCULAR_ALERT -> CHANNEL_CIRCULARS_ID
            NotificationType.EXAM_REMINDER, NotificationType.WEAKNESS_ALERT -> CHANNEL_EXAMS_ID
            else -> CHANNEL_STUDY_ID
        }

        try {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager.notify(notification.id.hashCode(), builder.build())
        } catch (e: Exception) {
            // Ignore if notification permission is not granted on Android 13+
        }
    }
}
