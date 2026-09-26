package ru.homeroute.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object RouteLauncher {
    private const val YANDEX_MAPS_PACKAGE = "ru.yandex.yandexmaps"
    private const val BUILD_ROUTE_ACTION =
        "ru.yandex.yandexmaps.action.BUILD_ROUTE_ON_MAP"

    private const val DEST_LAT = 55.768603
    private const val DEST_LON = 49.148222

    fun open(context: Context) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                locationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            openRoute(
                                context,
                                location.latitude,
                                location.longitude
                            )
                        } else {
                            getFreshLocation(context, locationClient)
                        }
                    }
                    .addOnFailureListener {
                        openRouteToDestination(context)
                    }
                return
            } catch (_: SecurityException) {
                // Ниже будет запасной вариант.
            }
        }

        openRouteToDestination(context)
    }

    private fun getFreshLocation(
        context: Context,
        locationClient: com.google.android.gms.location.FusedLocationProviderClient
    ) {
        try {
            locationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    openRoute(
                        context,
                        location.latitude,
                        location.longitude
                    )
                } else {
                    openRouteToDestination(context)
                }
            }.addOnFailureListener {
                openRouteToDestination(context)
            }
        } catch (_: SecurityException) {
            openRouteToDestination(context)
        }
    }

    private fun openRoute(
        context: Context,
        fromLat: Double,
        fromLon: Double
    ) {
        val intent = Intent(BUILD_ROUTE_ACTION).apply {
            setPackage(YANDEX_MAPS_PACKAGE)
            putExtra("lat_from", fromLat)
            putExtra("lon_from", fromLon)
            putExtra("lat_to", DEST_LAT)
            putExtra("lon_to", DEST_LON)
            putExtra("show_jams", 1)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            openRouteToDestination(context)
        }
    }

    private fun openRouteToDestination(context: Context) {
        val intent = Intent(BUILD_ROUTE_ACTION).apply {
            setPackage(YANDEX_MAPS_PACKAGE)
            putExtra("lat_to", DEST_LAT)
            putExtra("lon_to", DEST_LON)
            putExtra("show_jams", 1)
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
