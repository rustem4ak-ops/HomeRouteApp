package ru.homeroute.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeRouteScreen() }
    }

    @Composable
    private fun HomeRouteScreen() {
        val context = this
        val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

        var destination by remember { mutableStateOf(AppPrefs.destination(context)) }
        var radius by remember { mutableFloatStateOf(AppPrefs.radius(context).toFloat()) }
        var enabled by remember { mutableStateOf(AppPrefs.isEnabled(context)) }
        var status by remember {
            mutableStateOf(
                if (AppPrefs.hasHome(context)) "Дом установлен. Отслеживание готово."
                else "Сначала установите точку дома."
            )
        }

        val notificationPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        val locationPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            val fineGranted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || hasFineLocation()
            status = if (fineGranted) {
                "Геолокация разрешена. Теперь установите дом."
            } else {
                "Разрешите точную геолокацию."
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("HomeRoute", style = MaterialTheme.typography.headlineMedium)
            Text("Уведомление при выезде из дома и быстрый маршрут до KazanMall")

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("1. Точка дома", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(if (AppPrefs.hasHome(context)) "Дом установлен" else "Нажмите кнопку, находясь дома.")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (!hasFineLocation()) {
                                locationPermission.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                saveCurrentLocation(
                                    fused,
                                    { lat, lon ->
                                        AppPrefs.saveHome(context, lat, lon)
                                        GeofenceManager.register(context)
                                        status = "Дом сохранён. Радиус " + radius.toInt() + " м."
                                    },
                                    { error -> status = error }
                                )
                            }
                        },
                        Modifier.fillMaxWidth()
                    ) {
                        Text("Установить дом по моей геопозиции")
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("2. Радиус дома: " + radius.toInt() + " м")
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 100f..300f,
                        steps = 3
                    )
                    Text("Для начала можно оставить 150 м.")
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("3. Пункт назначения", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Куда ехать") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("KazanMall: Казань, ул. Павлюхина, 91")
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Отслеживать выезд")
                    Text("После возвращения домой уведомление снова активируется.")
                }
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Button(
                onClick = {
                    AppPrefs.saveSettings(context, radius.toInt(), destination, enabled)
                    if (enabled) GeofenceManager.register(context)
                    status = if (enabled) "Настройки сохранены. Отслеживание включено."
                    else "Отслеживание выключено."
                },
                Modifier.fillMaxWidth()
            ) {
                Text("Сохранить настройки")
            }

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    openBackgroundLocationSettings()
                },
                Modifier.fillMaxWidth()
            ) {
                Text("Разрешения для работы в фоне")
            }

            Button(
                onClick = { RouteLauncher.open(context) },
                Modifier.fillMaxWidth()
            ) {
                Text("Открыть маршрут сейчас")
            }

            Text(status, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Для автоматического срабатывания Android должен разрешить постоянный доступ к геолокации. Система может немного задерживать геозону из-за энергосбережения.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    private fun hasFineLocation(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun openBackgroundLocationSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:" + packageName)
            )
        )
    }

    private fun saveCurrentLocation(
        fused: com.google.android.gms.location.FusedLocationProviderClient,
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasFineLocation()) {
            onError("Нет разрешения на точную геолокацию.")
            return
        }
        fused.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    onError("Не удалось получить геопозицию. Включите GPS и попробуйте ещё раз.")
                }
            }
            .addOnFailureListener {
                onError("Ошибка геолокации.")
            }
    }
}
