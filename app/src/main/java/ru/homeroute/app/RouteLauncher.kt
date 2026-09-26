package ru.homeroute.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object RouteLauncher {
    private const val DEST_LAT = 55.768603
    private const val DEST_LON = 49.148222

    fun open(context: Context) {
        // Используем официальный HTTPS-формат Яндекс Карт как основной.
        // В отличие от yandexmaps:// этот URL явно содержит режим маршрута.
        // ~ перед координатами означает старт от текущего местоположения.
        val routeUrl =
            "https://yandex.ru/maps/?mode=routes&rtext=~$DEST_LAT,$DEST_LON&rtt=auto"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(routeUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Не удалось открыть маршрут в Яндекс Картах",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
