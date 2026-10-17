package com.example.ui.tools

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PoliceNavy
import com.example.util.AppLanguage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class VehicleDocumentItem(
    val id: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val documentType: String, // Revenue License, Insurance, Driving License, Service
    val expiryDate: String,   // yyyy-MM-dd
    val referenceNumber: String = "",
    val notes: String = ""
)

@Composable
fun VehicleDocumentReminderView(
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vehicle_reminders_prefs", Context.MODE_PRIVATE) }
    var documents by remember { mutableStateOf(loadDocumentsFromPrefs(prefs)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDoc by remember { mutableStateOf<VehicleDocumentItem?>(null) }

    fun saveDocuments(newList: List<VehicleDocumentItem>) {
        documents = newList
        saveDocumentsToPrefs(prefs, newList)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Vehicle",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "රියදුරු ලේඛන මතක් කිරීම් (Vehicle Reminders)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                            )
                            Text(
                                text = "ආදායම් බලපත්‍රය, රක්ෂණය හා රියදුරු බලපත්‍ර කල් ඉකුත් වීමට පෙර දැනුවත් වන්න.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            )
                        }
                    }
                }
            }

            if (documents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "තවමත් ලේඛන ඇතුළත් කර නොමැත",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "පහළ ඇති + බොත්තම ඔබා ඔබගේ වාහනයේ ආදායම් බලපත්‍රය හෝ රක්ෂණ විස්තර එක් කරන්න.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(documents, key = { it.id }) { doc ->
                    VehicleDocCard(
                        doc = doc,
                        onEdit = {
                            editingDoc = doc
                            showAddDialog = true
                        },
                        onDelete = {
                            val updated = documents.filter { it.id != doc.id }
                            saveDocuments(updated)
                            Toast.makeText(context, "ලේඛනය ඉවත් කරන ලදී", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add New Document
        FloatingActionButton(
            onClick = {
                editingDoc = null
                showAddDialog = true
            },
            containerColor = PoliceNavy,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "අලුත් ලේඛනයක්", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showAddDialog) {
        AddEditVehicleDocDialog(
            initialDoc = editingDoc,
            onDismiss = {
                showAddDialog = false
                editingDoc = null
            },
            onSave = { savedDoc ->
                val current = documents.toMutableList()
                val idx = current.indexOfFirst { it.id == savedDoc.id }
                if (idx >= 0) {
                    current[idx] = savedDoc
                } else {
                    current.add(0, savedDoc)
                }
                saveDocuments(current)
                showAddDialog = false
                editingDoc = null
                Toast.makeText(context, "ලේඛන විස්තර සාර්ථකව සුරැකිණි!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun VehicleDocCard(
    doc: VehicleDocumentItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val daysLeft = calculateDaysRemaining(doc.expiryDate)
    val statusColor = when {
        daysLeft < 0 -> Color(0xFFDC2626) // Expired Red
        daysLeft <= 30 -> Color(0xFFEA580C) // Warning Orange
        else -> Color(0xFF16A34A) // Safe Green
    }

    val statusText = when {
        daysLeft < 0 -> "කල් ඉකුත් වී ඇත (${-daysLeft} days ago)"
        daysLeft == 0L -> "අද කල් ඉකුත් වේ (Expires Today!)"
        daysLeft == 1L -> "හෙට කල් ඉකුත් වේ (Tomorrow)"
        daysLeft <= 30 -> "තව දින $daysLeft යි (Expiring Soon!)"
        else -> "තව දින $daysLeft ක් ඇත (Valid)"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (doc.vehicleType.contains("Bike", ignoreCase = true)) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                        contentDescription = doc.vehicleType,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.vehicleNumber.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${doc.vehicleType} • ${doc.documentType}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Days left badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (daysLeft < 0) "EXPIRED" else "දින $daysLeft",
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "කල් ඉකුත් වන දිනය (Expiry): ${doc.expiryDate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                    if (doc.referenceNumber.isNotBlank()) {
                        Text(
                            text = "අංකය/Policy No: ${doc.referenceNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (doc.notes.isNotBlank()) {
                        Text(
                            text = "සටහන්: ${doc.notes}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PoliceNavy, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditVehicleDocDialog(
    initialDoc: VehicleDocumentItem?,
    onDismiss: () -> Unit,
    onSave: (VehicleDocumentItem) -> Unit
) {
    val context = LocalContext.current
    var vehicleNumber by remember { mutableStateOf(initialDoc?.vehicleNumber ?: "") }
    var vehicleType by remember { mutableStateOf(initialDoc?.vehicleType ?: "Car (මෝටර් රථය)") }
    var documentType by remember { mutableStateOf(initialDoc?.documentType ?: "Revenue License (ආදායම් බලපත්‍රය)") }
    var expiryDate by remember { mutableStateOf(initialDoc?.expiryDate ?: "") }
    var refNumber by remember { mutableStateOf(initialDoc?.referenceNumber ?: "") }
    var notes by remember { mutableStateOf(initialDoc?.notes ?: "") }

    val vehicleTypes = listOf("Car (මෝටර් රථය)", "Motorbike (යතුරුපැදිය)", "Three-Wheeler (ත්‍රිරෝද රථය)", "Van / Lorry (වෑන්/ලොරි)", "Bus (බස් රථය)")
    val documentTypes = listOf(
        "Revenue License (ආදායම් බලපත්‍රය)",
        "Vehicle Insurance (වාහන රක්ෂණය)",
        "Driving License (රියදුරු බලපත්‍රය)",
        "Next Service (මීළඟ සේවා වාරය)",
        "Fitness Certificate (යෝග්‍යතා සහතිකය)"
    )

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (expiryDate.isNotBlank()) {
            try {
                val parts = expiryDate.split("-")
                if (parts.size == 3) {
                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (ignored: Exception) {}
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val m = (month + 1).toString().padStart(2, '0')
                val d = dayOfMonth.toString().padStart(2, '0')
                expiryDate = "$year-$m-$d"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialDoc == null) "නව රියදුරු ලේඛනයක් එක් කරන්න" else "ලේඛන විස්තර සංස්කරණය",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("වාහන අංකය (උදා: WP CAB-1234)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("වාහන වර්ගය (Vehicle Type):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column {
                        vehicleTypes.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { vehicleType = type }
                                    .padding(vertical = 4.dp)
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = vehicleType == type,
                                    onClick = { vehicleType = type }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = type, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text("ලේඛන වර්ගය (Document):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column {
                        documentTypes.forEach { doc ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { documentType = doc }
                                    .padding(vertical = 4.dp)
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = documentType == doc,
                                    onClick = { documentType = doc }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = doc, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Surface(
                        onClick = { showDatePicker() },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("කල් ඉකුත් වන දිනය (Expiry Date)", fontSize = 11.sp, color = Color(0xFF1E40AF))
                                Text(
                                    text = if (expiryDate.isBlank()) "දිනය තෝරන්න (Select Date)" else expiryDate,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                            Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = Color(0xFF2563EB))
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = refNumber,
                        onValueChange = { refNumber = it },
                        label = { Text("සහතික / රක්ෂණ අංකය (Policy/License No)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("අමතර සටහන් (Notes)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (vehicleNumber.isBlank()) {
                        Toast.makeText(context, "කරුණාකර වාහන අංකය ඇතුළත් කරන්න", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (expiryDate.isBlank()) {
                        Toast.makeText(context, "කරුණාකර කල් ඉකුත් වන දිනය තෝරන්න", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val item = VehicleDocumentItem(
                        id = initialDoc?.id ?: "doc_${System.currentTimeMillis()}",
                        vehicleNumber = vehicleNumber.trim(),
                        vehicleType = vehicleType,
                        documentType = documentType,
                        expiryDate = expiryDate,
                        referenceNumber = refNumber.trim(),
                        notes = notes.trim()
                    )
                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy)
            ) {
                Text("සුරකින්න")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("අවලංගුයි")
            }
        }
    )
}

fun calculateDaysRemaining(expiryDateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expiry = sdf.parse(expiryDateStr) ?: return 0L
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = expiry.time - todayCalendar.timeInMillis
        TimeUnit.MILLISECONDS.toDays(diffMs)
    } catch (e: Exception) {
        0L
    }
}

private fun loadDocumentsFromPrefs(prefs: android.content.SharedPreferences): List<VehicleDocumentItem> {
    val json = prefs.getString("vehicle_docs_json", null)
    if (json.isNullOrBlank()) {
        // Sample default templates for first run
        return listOf(
            VehicleDocumentItem(
                id = "doc_sample_rev",
                vehicleNumber = "WP CAB-4521",
                vehicleType = "Car (මෝටර් රථය)",
                documentType = "Revenue License (ආදායම් බලපත්‍රය)",
                expiryDate = "2026-11-20",
                referenceNumber = "RL-2025-98421",
                notes = "Western Province DMT Werahera"
            ),
            VehicleDocumentItem(
                id = "doc_sample_ins",
                vehicleNumber = "WP CAB-4521",
                vehicleType = "Car (මෝටර් රථය)",
                documentType = "Vehicle Insurance (වාහන රක්ෂණය)",
                expiryDate = "2026-12-15",
                referenceNumber = "SLIC-V-7845210",
                notes = "Sri Lanka Insurance Comprehensive"
            ),
            VehicleDocumentItem(
                id = "doc_sample_lic",
                vehicleNumber = "B4125894",
                vehicleType = "Driving License",
                documentType = "Driving License (රියදුරු බලපත්‍රය)",
                expiryDate = "2028-06-30",
                referenceNumber = "B-4125894-L",
                notes = "Class B & B1 (Auto/Manual)"
            )
        )
    }
    return try {
        val list = mutableListOf<VehicleDocumentItem>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                VehicleDocumentItem(
                    id = obj.getString("id"),
                    vehicleNumber = obj.getString("vehicleNumber"),
                    vehicleType = obj.getString("vehicleType"),
                    documentType = obj.getString("documentType"),
                    expiryDate = obj.getString("expiryDate"),
                    referenceNumber = obj.optString("referenceNumber", ""),
                    notes = obj.optString("notes", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

private fun saveDocumentsToPrefs(prefs: android.content.SharedPreferences, list: List<VehicleDocumentItem>) {
    try {
        val arr = JSONArray()
        for (doc in list) {
            val obj = JSONObject().apply {
                put("id", doc.id)
                put("vehicleNumber", doc.vehicleNumber)
                put("vehicleType", doc.vehicleType)
                put("documentType", doc.documentType)
                put("expiryDate", doc.expiryDate)
                put("referenceNumber", doc.referenceNumber)
                put("notes", doc.notes)
            }
            arr.put(obj)
        }
        prefs.edit().putString("vehicle_docs_json", arr.toString()).apply()
    } catch (ignored: Exception) {}
}
