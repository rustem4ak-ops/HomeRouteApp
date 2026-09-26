package ru.homeroute.app

import android.app.Activity
import android.os.Bundle

class RouteOpenActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RouteLauncher.open(this)
        finish()
    }
}
