package com.example.core.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.database.UserPreferences
import com.example.core.domain.DailyPrayerSchedule
import java.util.Calendar

object NotificationChannels {
    const val CHANNEL_PRAYER = "noor_prayer_reminders"
    const val CHANNEL_AZKAR = "noor_azkar_reminders"
    const val CHANNEL_READING = "noor_reading_reminders"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER,
                "تنبيهات مواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات اقتراب وحلول أوقات الصلوات الخمس"
            }
            val azkarChannel = NotificationChannel(
                CHANNEL_AZKAR,
                "تذكير الأذكار اليومية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير بأذكار الصباح والمساء"
            }
            val readingChannel = NotificationChannel(
                CHANNEL_READING,
                "تذكير الورد القرآني",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير بالقراءة اليومية للقرآن الكريم"
            }
            nm.createNotificationChannel(prayerChannel)
            nm.createNotificationChannel(azkarChannel)
            nm.createNotificationChannel(readingChannel)
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.ensureCreated(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val title = intent.getStringExtra("title") ?: "نور الهدى"
        val body = intent.getStringExtra("body") ?: "تذكير"
        val channelId = intent.getStringExtra("channelId") ?: NotificationChannels.CHANNEL_PRAYER
        val playSound = intent.getBooleanExtra("playSound", false)
        val notifId = intent.getIntExtra("notifId", 1001)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notifId,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (playSound) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (_: SecurityException) {
            // Permission revoked
        }
    }
}

object ReminderScheduler {
    fun syncAllReminders(
        context: Context,
        prefs: UserPreferences,
        schedule: DailyPrayerSchedule
    ) {
        NotificationChannels.ensureCreated(context)
        val prayers = listOf(
            Triple(101, "الفجر" to schedule.fajr, prefs.notifyFajr),
            Triple(102, "الظهر" to schedule.dhuhr, prefs.notifyDhuhr),
            Triple(103, "العصر" to schedule.asr, prefs.notifyAsr),
            Triple(104, "المغرب" to schedule.maghrib, prefs.notifyMaghrib),
            Triple(105, "العشاء" to schedule.isha, prefs.notifyIsha)
        )

        for ((id, nameTime, enabled) in prayers) {
            val (name, time) = nameTime
            if (enabled) {
                val parts = time.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull()
                val m = parts.getOrNull(1)?.toIntOrNull()
                if (h != null && m != null) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        add(Calendar.MINUTE, -prefs.prePrayerAlertMinutes)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                    val subtitle = if (prefs.prePrayerAlertMinutes > 0) {
                        "تبقى ${prefs.prePrayerAlertMinutes} دقائق على أذان $name ($time)"
                    } else {
                        "حان الآن موعد أذان $name ($time)"
                    }
                    scheduleAlarm(
                        context = context,
                        requestCode = id,
                        triggerAtMillis = cal.timeInMillis,
                        title = "موعد صلاة $name",
                        body = subtitle,
                        channelId = NotificationChannels.CHANNEL_PRAYER,
                        playSound = prefs.adhanSoundEnabled
                    )
                }
            } else {
                cancelAlarm(context, id)
            }
        }

        if (prefs.notifyMorningAzkar) {
            scheduleDailyHour(context, 201, 6, 0, "أذكار الصباح", "ابدأ يومك بذكر الله وحصن المسلم", NotificationChannels.CHANNEL_AZKAR)
        } else cancelAlarm(context, 201)

        if (prefs.notifyEveningAzkar) {
            scheduleDailyHour(context, 202, 17, 0, "أذكار المساء", "لا تنسَ أذكار المساء والتحصين اليومي", NotificationChannels.CHANNEL_AZKAR)
        } else cancelAlarm(context, 202)

        if (prefs.notifyDailyReading) {
            scheduleDailyHour(context, 203, 20, 30, "الورد القرآني اليومي", "حافظ على وردك اليومي من القرآن الكريم في نور الهدى", NotificationChannels.CHANNEL_READING)
        } else cancelAlarm(context, 203)
    }

    private fun scheduleDailyHour(
        context: Context,
        requestCode: Int,
        hour: Int,
        minute: Int,
        title: String,
        body: String,
        channelId: String
    ) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        scheduleAlarm(context, requestCode, cal.timeInMillis, title, body, channelId, false)
    }

    private fun scheduleAlarm(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        body: String,
        channelId: String,
        playSound: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
            putExtra("channelId", channelId)
            putExtra("playSound", playSound)
            putExtra("notifId", requestCode)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    private fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pending != null) {
            alarmManager.cancel(pending)
        }
    }
}
