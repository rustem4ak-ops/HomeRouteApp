package ru.homeroute.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object RouteLauncher {
    private const val YANDEX_MAPS_PACKAGE = "ru.yandex.yandexmaps"
    private const val DEST_LAT = 55.768603
    private const val DEST_LON = 49.148222

    fun open(context: Context) {
        // Важно: mode=routes заставляет Яндекс Карты открыть именно экран маршрута,
        // а не карточку/точку назначения. ~ перед координатами означает
        // "от текущего местоположения пользователя".
        val routeQuery = "mode=routes&rtext=~$DEST_LAT,$DEST_LON&rtt=auto"

        val appUri = Uri.parse("yandexmaps://maps.yandex.ru/?$routeQuery")
        val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
            setPackage(YANDEX_MAPS_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(appIntent)
            return
        } catch (_: Exception) {
        }

        // Веб-URL содержит те же параметры маршрута и может передать их
        // установленному приложению Яндекс Карт.
        val webUri = Uri.parse("https://yandex.ru/maps/?$routeQuery")
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Не удалось открыть маршрут в Яндекс Картах",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
