package com.example.ui.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PoliceNavy
import com.example.util.AppLanguage
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun PhoneSensorCheckerView(
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Accelerometer State
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }
    var hasAccelerometer by remember { mutableStateOf(false) }

    // Gyroscope State
    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }
    var hasGyroscope by remember { mutableStateOf(false) }

    // Light Sensor State
    var lightLux by remember { mutableFloatStateOf(0f) }
    var hasLightSensor by remember { mutableStateOf(false) }

    // Proximity Sensor State
    var proximityDistance by remember { mutableFloatStateOf(0f) }
    var isNear by remember { mutableStateOf(false) }
    var hasProximity by remember { mutableStateOf(false) }

    // Magnetometer / Compass State
    var compassHeading by remember { mutableFloatStateOf(0f) }
    var hasMagnetometer by remember { mutableStateOf(false) }

    // Battery Telemetry
    var batteryLevel by remember { mutableIntStateOf(0) }
    var batteryTempCelsius by remember { mutableFloatStateOf(0f) }
    var batteryVoltageMv by remember { mutableIntStateOf(0) }
    var batteryStatusText by remember { mutableStateOf("Checking...") }

    // Sensor Listeners registration
    DisposableEffect(sensorManager) {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        hasAccelerometer = (accelSensor != null)

        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        hasGyroscope = (gyroSensor != null)

        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        hasLightSensor = (lightSensor != null)

        val proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        hasProximity = (proxSensor != null)

        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        hasMagnetometer = (magSensor != null)

        val lastAccelerometer = FloatArray(3)
        val lastMagnetometer = FloatArray(3)
        var lastAccelSet = false
        var lastMagSet = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelX = event.values[0]
                        accelY = event.values[1]
                        accelZ = event.values[2]
                        System.arraycopy(event.values, 0, lastAccelerometer, 0, 3)
                        lastAccelSet = true
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroX = event.values[0]
                        gyroY = event.values[1]
                        gyroZ = event.values[2]
                    }
                    Sensor.TYPE_LIGHT -> {
                        lightLux = event.values[0]
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        proximityDistance = event.values[0]
                        val maxRange = event.sensor.maximumRange
                        isNear = (event.values[0] < maxRange)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, lastMagnetometer, 0, 3)
                        lastMagSet = true
                    }
                }

                if (lastAccelSet && lastMagSet) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, lastAccelerometer, lastMagnetometer)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuthRad = orientation[0]
                        val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360) % 360).toFloat()
                        compassHeading = azimuthDeg
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        gyroSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        proxSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        // Battery Receiver
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel = (level * 100) / scale
                }
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                if (temp > 0) {
                    batteryTempCelsius = temp / 10f
                }
                batteryVoltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                batteryStatusText = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging (ආරෝපණය වෙමින්)"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full (සම්පූර්ණයි)"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging (බැටරිය වැයවෙමින්)"
                    else -> "Battery Normal"
                }
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        onDispose {
            sensorManager.unregisterListener(listener)
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (ignored: Exception) {}
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Sensors",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "දුරකථන සංවේදක පරීක්ෂක (Sensor Checker)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "දුරකථනයේ Accelerometer, Compass, Light, Proximity හා Battery පරීක්ෂා කිරීම.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 1. Accelerometer (ත්වරණමානය)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = "Accel", tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("චලන සංවේදකය (Accelerometer)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        SensorStatusBadge(isAvailable = hasAccelerometer)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SensorAxisBox(label = "X-Axis (Tilt L/R)", value = String.format("%.2f m/s²", accelX), color = Color(0xFF2563EB))
                        SensorAxisBox(label = "Y-Axis (Tilt F/B)", value = String.format("%.2f m/s²", accelY), color = Color(0xFF16A34A))
                        SensorAxisBox(label = "Z-Axis (Gravity)", value = String.format("%.2f m/s²", accelZ), color = Color(0xFFD97706))
                    }
                }
            }
        }

        // 2. Compass & Magnetometer (මාලිමාව)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = "Compass", tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("මාලිමාව (Digital Compass)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        SensorStatusBadge(isAvailable = hasMagnetometer)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val headingInt = compassHeading.roundToInt()
                            val cardinal = when (headingInt) {
                                in 338..360, in 0..22 -> "North (උතුර)"
                                in 23..67 -> "North-East (ඊසාන)"
                                in 68..112 -> "East (නැගෙනහිර)"
                                in 113..157 -> "South-East (ගිණිකොන)"
                                in 158..202 -> "South (දකුණ)"
                                in 203..247 -> "South-West (නිරිත)"
                                in 248..292 -> "West (බස්නාහිර)"
                                in 293..337 -> "North-West (වයඹ)"
                                else -> "North"
                            }
                            Text(text = "$headingInt° $cardinal", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFDC2626))
                            Text(text = "දුරකථනය හරවමින් දිශාව පරීක්ෂා කරන්න", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Compass Visual Dial
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(54.dp)) {
                                drawCircle(color = Color(0xFFCBD5E1), style = Stroke(width = 3f))
                                val angleRad = Math.toRadians((compassHeading - 90).toDouble())
                                val radius = size.minDimension / 2.2f
                                val endX = center.x + (radius * Math.cos(angleRad)).toFloat()
                                val endY = center.y + (radius * Math.sin(angleRad)).toFloat()
                                drawLine(
                                    color = Color(0xFFDC2626),
                                    start = center,
                                    end = Offset(endX, endY),
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Ambient Light Sensor (ආලෝක සංවේදකය)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Light", tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ආලෝක සංවේදකය (Light Sensor)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        SensorStatusBadge(isAvailable = hasLightSensor)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val lightDesc = when {
                        lightLux < 10 -> "අඳුරු පරිසරයකි (Very Dark)"
                        lightLux < 200 -> "කාමර අභ්‍යන්තර ආලෝකය (Normal Indoor)"
                        lightLux < 1000 -> "දීප්තිමත් කාමර ආලෝකය (Bright Indoor)"
                        lightLux < 10000 -> "දවල් එළිමහන් ආලෝකය (Daylight)"
                        else -> "සෘජු හිරු එළිය (Direct Sunlight)"
                    }
                    Text(
                        text = "${lightLux.roundToInt()} Lux",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD97706)
                    )
                    Text(text = lightDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (lightLux / 1000f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }

        // 4. Proximity Sensor (සමීපතා සංවේදකය)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = "Prox", tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("සමීපතා සංවේදකය (Proximity)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isNear) "NEAR (ආසන්නයි / Screen Off Area)" else "FAR (දුරස්ථයි / Normal)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isNear) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                        Text(text = "තිරයේ ඉහළ කන් තබන තැන අත තබා බලන්න", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isNear) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isNear) "NEAR" else "FAR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (isNear) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                    }
                }
            }
        }

        // 5. Battery Status & Health (බැටරි සෞඛ්‍යය)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery", tint = Color(0xFF059669))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("බැටරි තොරතුරු (Battery Telemetry)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SensorAxisBox(label = "මට්ටම (Level)", value = "$batteryLevel%", color = Color(0xFF059669))
                        SensorAxisBox(label = "උෂ්ණත්වය (Temp)", value = "$batteryTempCelsius °C", color = if (batteryTempCelsius > 42f) Color(0xFFDC2626) else Color(0xFF0284C7))
                        SensorAxisBox(label = "වෝල්ටීයතාව (Volt)", value = "${batteryVoltageMv / 1000f} V", color = Color(0xFF6B7280))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "තත්ත්වය: $batteryStatusText", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 6. Vibration Test Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = "Vib", tint = PoliceNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("කම්පන පරීක්ෂාව (Vibration Test)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(text = "Haptic Vibration මෝටරය පරීක්ෂා කරන්න", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                                    vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(250)
                                }
                            } catch (ignored: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Vibrate", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SensorStatusBadge(isAvailable: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    ) {
        Text(
            text = if (isAvailable) "Active" else "N/A",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAvailable) Color(0xFF16A34A) else Color(0xFFDC2626),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SensorAxisBox(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
