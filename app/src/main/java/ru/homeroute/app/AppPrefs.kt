package ru.homeroute.app

import android.content.Context

object AppPrefs {
    private const val FILE = "home_route"
    private const val HOME_LAT = "home_lat"
    private const val HOME_LON = "home_lon"
    private const val RADIUS = "radius"
    private const val DESTINATION = "destination"
    private const val ARMED = "armed"
    private const val ENABLED = "enabled"

    private fun p(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun homeLat(context: Context) = p(context).getFloat(HOME_LAT, Float.NaN).toDouble()
    fun homeLon(context: Context) = p(context).getFloat(HOME_LON, Float.NaN).toDouble()
    fun hasHome(context: Context) = !homeLat(context).isNaN() && !homeLon(context).isNaN()
    fun radius(context: Context) = p(context).getInt(RADIUS, 150)
    fun destination(context: Context) = p(context).getString(DESTINATION, "KazanMall, Казань, ул. Павлюхина, 91")
        ?: "KazanMall, Казань, ул. Павлюхина, 91"
    fun isArmed(context: Context) = p(context).getBoolean(ARMED, true)
    fun isEnabled(context: Context) = p(context).getBoolean(ENABLED, true)

    fun saveHome(context: Context, lat: Double, lon: Double) {
        p(context).edit()
            .putFloat(HOME_LAT, lat.toFloat())
            .putFloat(HOME_LON, lon.toFloat())
            .putBoolean(ARMED, true)
            .apply()
    }

    fun saveSettings(context: Context, radius: Int, destination: String, enabled: Boolean) {
        p(context).edit()
            .putInt(RADIUS, radius)
            .putString(DESTINATION, destination)
            .putBoolean(ENABLED, enabled)
            .apply()
    }

    fun setArmed(context: Context, armed: Boolean) {
        p(context).edit().putBoolean(ARMED, armed).apply()
    }
}
