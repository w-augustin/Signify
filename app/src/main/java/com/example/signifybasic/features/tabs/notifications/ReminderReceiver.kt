package com.example.signifybasic.features.tabs.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
import com.example.signifybasic.R
import com.example.signifybasic.database.DBHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref = context.getSharedPreferences("NotificationSettings", Context.MODE_PRIVATE)

        if (!sharedPref.getBoolean("push_enabled", true)) {
            Log.d("ReminderReceiver", "Push notifications disabled. Skipping notification.")
            return
        }

        val soundName = sharedPref.getString("notification_sound", "Chime") ?: "Chime"
        val soundResId = when (soundName) {
            "Ding" -> R.raw.ding
            "Pop" -> R.raw.pop
            else -> R.raw.chime
        }

        val soundUri = Uri.parse("android.resource://${context.packageName}/$soundResId")

        val channelId = "reminder_channel_$soundName"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Reminders with $soundName",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Daily Reminder")
            .setContentText("It's time for your daily check-in!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }

        val dbHelper = DBHelper(context)
        dbHelper.insertNotification("It's time for your daily check-in!")

    }


}
