package ru.homeroute.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "departure"
        private const val NOTIFICATION_ID = 42017
    }

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        when (event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> AppPrefs.setArmed(context, true)
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                if (!AppPrefs.isEnabled(context) || !AppPrefs.isArmed(context)) return
                AppPrefs.setArmed(context, false)
                showDepartureNotification(context)
            }
        }
    }

    private fun showDepartureNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Выезд из дома",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return

        val pending = PendingIntent.getActivity(
            context,
            42018,
            Intent(context, RouteOpenActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Пора ехать")
            .setContentText("Открыть маршрут до KazanMall")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Нажмите, чтобы открыть Яндекс Карты и построить маршрут от текущего местоположения до KazanMall."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
