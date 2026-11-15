package com.rafiqaldhikr.app.ui.screens.qibla

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import com.rafiqaldhikr.app.core.prayer.Qibla
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import kotlin.math.abs

@Composable
fun QiblaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val city = remember { PrayerSettings.city(context) }
    val bearing = remember { Qibla.bearingDegrees(city.latitude, city.longitude) }
    val declination = remember {
        GeomagneticField(city.latitude.toFloat(), city.longitude.toFloat(), 0f, System.currentTimeMillis())
            .declination.toDouble()
    }

    var heading by remember { mutableDoubleStateOf(0.0) }   // degrees clockwise from true north
    var accuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    DisposableEffect(rotationSensor) {
        var smoothed: Double? = null
        val listener = object : SensorEventListener {
            private val rotation = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                val azimuth = (Math.toDegrees(orientation[0].toDouble()) + declination + 360.0) % 360.0
                // Low-pass filter on the circle so the needle does not jitter or jump at 0/360.
                val prev = smoothed
                val next = if (prev == null) azimuth else (prev + 0.15 * Qibla.shortestDelta(prev, azimuth) + 360.0) % 360.0
                smoothed = next
                heading = next
            }

            override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
                accuracy = newAccuracy
            }
        }
        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    val delta = Qibla.shortestDelta(heading, bearing)
    val aligned = rotationSensor != null && abs(delta) <= 3.0
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val needle = if (aligned) Color(0xFF2E7D32) else primary

    Scaffold(topBar = { RafiqTopBar(title = "اتجاه القبلة", onBackClick = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "المدينة: ${city.nameAr} — القبلة %d° من الشمال".format(bearing.toInt()),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (rotationSensor == null) {
                Text(
                    "هذا الجهاز لا يملك حساس اتجاه، فلا يمكن عرض البوصلة. اتجاه القبلة من ${city.nameAr} هو " +
                        "%d° من الشمال الحقيقي (باتجاه عقارب الساعة).".format(bearing.toInt()),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension / 2f - 8f
                    drawCircle(outline, radius = r, center = c, style = Stroke(width = 4f))
                    // The dial turns with the phone so that "N" always points to true north.
                    rotate(degrees = (-heading).toFloat(), pivot = c) {
                        drawCircle(outline, radius = 6f, center = Offset(c.x, c.y - r))
                        // The needle points to the Qibla on the dial.
                        rotate(degrees = bearing.toFloat(), pivot = c) {
                            val path = Path().apply {
                                moveTo(c.x, c.y - r * 0.85f)
                                lineTo(c.x - r * 0.12f, c.y)
                                lineTo(c.x + r * 0.12f, c.y)
                                close()
                            }
                            drawPath(path, needle)
                            drawCircle(needle, radius = 10f, center = c)
                        }
                    }
                }
                Text(
                    if (aligned) "أنت تواجه القبلة" else "أدر الهاتف حتى يشير السهم إلى الأعلى",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (aligned) needle else MaterialTheme.colorScheme.onSurface
                )
                if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                    Text(
                        "دقة البوصلة منخفضة: حرّك الهاتف على شكل رقم ٨ في الهواء لمعايرتها، وابتعد عن المعادن والمغناطيس.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "تعتمد على المدينة المختارة في إعدادات المواقيت، وتقريبية؛ تحقق بعلامة المسجد أو الأدلة إن أمكن.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
