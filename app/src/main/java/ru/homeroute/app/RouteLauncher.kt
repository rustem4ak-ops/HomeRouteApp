package ru.homeroute.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object RouteLauncher {
    private const val YANDEX_MAPS_PACKAGE = "ru.yandex.yandexmaps"
    private const val DEST_LAT = 55.768603
    private const val DEST_LON = 49.148222

    fun open(context: Context) {
        val destination = AppPrefs.destination(context)
        val encoded = URLEncoder.encode(destination, "UTF-8")
        val appUri = Uri.parse(
            "yandexmaps://maps.yandex.ru/?rtext=~$DEST_LAT,$DEST_LON&rtt=auto&text=$encoded"
        )
        val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
            setPackage(YANDEX_MAPS_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(appIntent)
            return
        } catch (_: Exception) {
        }
        val webUri = Uri.parse(
            "https://yandex.ru/maps/?rtext=~$DEST_LAT,$DEST_LON&rtt=auto&text=$encoded"
        )
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(context, "Не удалось открыть Яндекс Карты", Toast.LENGTH_LONG).show()
        }
    }
}
