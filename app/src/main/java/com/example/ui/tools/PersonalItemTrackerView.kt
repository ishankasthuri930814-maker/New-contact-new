package com.example.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PoliceNavy
import com.example.util.AppLanguage
import org.json.JSONArray
import org.json.JSONObject

data class PersonalItemRecord(
    val id: String,
    val title: String,
    val identifierValue: String,
    val category: String, // Identity, Banking, Utilities, Devices, Vehicle, Other
    val holderName: String = "",
    val notes: String = ""
)

@Composable
fun PersonalItemTrackerView(
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("personal_tracker_prefs", Context.MODE_PRIVATE) }
    var itemsList by remember { mutableStateOf(loadPersonalItemsFromPrefs(prefs)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PersonalItemRecord?>(null) }

    fun saveItems(newList: List<PersonalItemRecord>) {
        itemsList = newList
        savePersonalItemsToPrefs(prefs, newList)
    }

    val categories = listOf("All", "Identity (හැඳුනුම්පත්)", "Banking (බැංකු)", "Utilities (බිල්පත්)", "Devices (උපකරණ)", "Other (වෙනත්)")

    val filtered = itemsList.filter { item ->
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "Identity (හැඳුනුම්පත්)" -> item.category.startsWith("Identity")
            "Banking (බැංකු)" -> item.category.startsWith("Banking")
            "Utilities (බිල්පත්)" -> item.category.startsWith("Utilities")
            "Devices (උපකරණ)" -> item.category.startsWith("Devices")
            else -> true
        }
        val q = searchQuery.trim().lowercase()
        val matchesQuery = if (q.isEmpty()) true else {
            item.title.lowercase().contains(q) ||
                    item.identifierValue.lowercase().contains(q) ||
                    item.holderName.lowercase().contains(q) ||
                    item.notes.lowercase().contains(q)
        }
        matchesCategory && matchesQuery
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
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
                                .background(Color(0xFF16A34A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Vault",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "වැදගත් තොරතුරු සුරැකුම (Personal Vault)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF14532D)
                                )
                            )
                            Text(
                                text = "ජාතික හැඳුනුම්පත්, පාස්පෝට්, බැංකු, විදුලි/ජල බිල්පත් අංක එකම තැනක සුරක්ෂිතව තබාගන්න.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF16A34A)
                                )
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("අංකය හෝ නම සොයන්න (Search saved numbers)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
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
                            Icon(Icons.Default.FolderSpecial, contentDescription = "Empty", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "තොරතුරු හමු නොවීය", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "පහළ ඇති + බොත්තම ඔබා ඔබගේ වැදගත් අංකයක් මෙහි සුරක්ෂිතව තබා ගන්න.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { item ->
                    PersonalItemCard(
                        item = item,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(item.title, item.identifierValue)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "${item.title} පිටපත් කරගන්නා ලදී (Copied)!", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = {
                            editingItem = item
                            showAddDialog = true
                        },
                        onDelete = {
                            val updated = itemsList.filter { it.id != item.id }
                            saveItems(updated)
                            Toast.makeText(context, "තොරතුර ඉවත් කරන ලදී", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Floating Action Button
        FloatingActionButton(
            onClick = {
                editingItem = null
                showAddDialog = true
            },
            containerColor = Color(0xFF16A34A),
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
                Text(text = "අලුත් තොරතුරක්", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showAddDialog) {
        AddEditPersonalItemDialog(
            initialItem = editingItem,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onSave = { savedItem ->
                val current = itemsList.toMutableList()
                val idx = current.indexOfFirst { it.id == savedItem.id }
                if (idx >= 0) {
                    current[idx] = savedItem
                } else {
                    current.add(0, savedItem)
                }
                saveItems(current)
                showAddDialog = false
                editingItem = null
                Toast.makeText(context, "තොරතුරු සාර්ථකව සුරැකිණි!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun PersonalItemCard(
    item: PersonalItemRecord,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when {
        item.category.startsWith("Identity") -> Icons.Default.Badge
        item.category.startsWith("Banking") -> Icons.Default.AccountBalance
        item.category.startsWith("Utilities") -> Icons.Default.Receipt
        item.category.startsWith("Devices") -> Icons.Default.Devices
        item.category.startsWith("Vehicle") -> Icons.Default.DirectionsCar
        else -> Icons.Default.FolderSpecial
    }

    val iconColor = when {
        item.category.startsWith("Identity") -> Color(0xFF2563EB)
        item.category.startsWith("Banking") -> Color(0xFF059669)
        item.category.startsWith("Utilities") -> Color(0xFFD97706)
        item.category.startsWith("Devices") -> Color(0xFF7C3AED)
        else -> PoliceNavy
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = item.category, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.holderName.isNotBlank()) {
                        Text(
                            text = item.holderName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = onCopy,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF0F2A4A), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2A4A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // The Key Value Highlight Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopy() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.identifierValue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(text = "Tap to copy", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "සටහන්: ${item.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PoliceNavy, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditPersonalItemDialog(
    initialItem: PersonalItemRecord?,
    onDismiss: () -> Unit,
    onSave: (PersonalItemRecord) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var value by remember { mutableStateOf(initialItem?.identifierValue ?: "") }
    var holder by remember { mutableStateOf(initialItem?.holderName ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Identity (හැඳුනුම්පත්)") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    val catOptions = listOf(
        "Identity (හැඳුනුම්පත්)",
        "Banking (බැංකු)",
        "Utilities (බිල්පත්)",
        "Devices (උපකරණ)",
        "Other (වෙනත්)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "නව වැදගත් තොරතුරක් සුරකින්න" else "තොරතුරු සංස්කරණය",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("නම / ලේබලය (උදා: මගේ NIC, BOC ගිණුම)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("අංකය / කේතය (Number / ID Value)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = holder,
                        onValueChange = { holder = it },
                        label = { Text("හිමිකරු / ආයතනය (Holder / Bank / Issuer)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("කාණ්ඩය (Category):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column {
                        catOptions.forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { category = cat }
                                    .padding(vertical = 4.dp)
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = category == cat,
                                    onClick = { category = cat }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = cat, fontSize = 12.sp)
                            }
                        }
                    }
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
                    if (title.isBlank() || value.isBlank()) {
                        Toast.makeText(context, "කරුණාකර නම සහ අංකය ඇතුළත් කරන්න", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val record = PersonalItemRecord(
                        id = initialItem?.id ?: "item_${System.currentTimeMillis()}",
                        title = title.trim(),
                        identifierValue = value.trim(),
                        holderName = holder.trim(),
                        category = category,
                        notes = notes.trim()
                    )
                    onSave(record)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
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

private fun loadPersonalItemsFromPrefs(prefs: android.content.SharedPreferences): List<PersonalItemRecord> {
    val json = prefs.getString("personal_items_json", null)
    if (json.isNullOrBlank()) {
        // Helpful first-time starter templates
        return listOf(
            PersonalItemRecord(
                id = "item_sample_nic",
                title = "ජාතික හැඳුනුම්පත (NIC)",
                identifierValue = "199308140000",
                category = "Identity (හැඳුනුම්පත්)",
                holderName = "මගේ ජාතික හැඳුනුම්පත",
                notes = "RPD Colombo Issued"
            ),
            PersonalItemRecord(
                id = "item_sample_dl",
                title = "රියදුරු බලපත්‍රය (Driving License)",
                identifierValue = "B-4125894",
                category = "Identity (හැඳුනුම්පත්)",
                holderName = "DMT Werahera",
                notes = "Class B & B1 (Auto/Manual)"
            ),
            PersonalItemRecord(
                id = "item_sample_electricity",
                title = "විදුලි බිල්පත් ගිණුම (CEB Account)",
                identifierValue = "0458129034",
                category = "Utilities (බිල්පත්)",
                holderName = "Ceylon Electricity Board",
                notes = "නිවසේ විදුලි බිල (House Bill)"
            ),
            PersonalItemRecord(
                id = "item_sample_water",
                title = "ජල බිල්පත් ගිණුම (Water Board)",
                identifierValue = "12/45/890/11",
                category = "Utilities (බිල්පත්)",
                holderName = "NWSDB",
                notes = "ජල සම්පාදන මණ්ඩලය"
            )
        )
    }
    return try {
        val list = mutableListOf<PersonalItemRecord>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                PersonalItemRecord(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    identifierValue = obj.getString("identifierValue"),
                    category = obj.getString("category"),
                    holderName = obj.optString("holderName", ""),
                    notes = obj.optString("notes", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

private fun savePersonalItemsToPrefs(prefs: android.content.SharedPreferences, list: List<PersonalItemRecord>) {
    try {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("identifierValue", item.identifierValue)
                put("category", item.category)
                put("holderName", item.holderName)
                put("notes", item.notes)
            }
            arr.put(obj)
        }
        prefs.edit().putString("personal_items_json", arr.toString()).apply()
    } catch (ignored: Exception) {}
}
