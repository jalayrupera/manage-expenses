package com.jalay.manageexpenses.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jalay.manageexpenses.MainActivity
import com.jalay.manageexpenses.R
import com.jalay.manageexpenses.domain.model.RecurringTransaction

object NotificationHelper {
    const val CHANNEL_ID_RECURRING = "recurring_reminders"
    private const val CHANNEL_NAME = "Recurring Payment Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for recurring payments due"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID_RECURRING,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showRecurringReminder(
        context: Context,
        transaction: RecurringTransaction
    ) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            transaction.id?.toInt() ?: 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECURRING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Payment Due: ${transaction.name}")
            .setContentText("₹${FormatUtils.formatAmount(transaction.amount)} - ${transaction.category}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            transaction.id?.toInt() ?: System.currentTimeMillis().toInt(),
            notification
        )
    }
}
