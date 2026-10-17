package com.example.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PoliceNavy
import com.example.util.AppLanguage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CashDenomination(
    val value: Int,
    val isCoin: Boolean = false,
    val labelSinhala: String,
    val color: Color
)

@Composable
fun DailyCashCalculatorView(
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val denominations = remember {
        listOf(
            // Sri Lankan Currency Notes
            CashDenomination(5000, false, "රු. 5,000", Color(0xFFC2410C)), // Orange/Gold
            CashDenomination(1000, false, "රු. 1,000", Color(0xFF0369A1)), // Cyan/Blue
            CashDenomination(500, false, "රු. 500", Color(0xFF7E22CE)),  // Purple
            CashDenomination(100, false, "රු. 100", Color(0xFFBE185D)),  // Pink/Orange
            CashDenomination(50, false, "රු. 50", Color(0xFF15803D)),   // Green
            CashDenomination(20, false, "රු. 20", Color(0xFF854D0E)),   // Brown
            // Sri Lankan Coins
            CashDenomination(10, true, "රු. 10 කාසිය", Color(0xFF475569)),
            CashDenomination(5, true, "රු. 5 කාසිය", Color(0xFF64748B)),
            CashDenomination(2, true, "රු. 2 කාසිය", Color(0xFF94A3B8)),
            CashDenomination(1, true, "රු. 1 කාසිය", Color(0xFFCBD5E1))
        )
    }

    val countMap = remember {
        mutableStateMapOf<Int, Int>().apply {
            denominations.forEach { put(it.value, 0) }
        }
    }

    var grandTotal = 0L
    var totalNotes = 0
    var totalCoins = 0

    denominations.forEach { denom ->
        val count = countMap[denom.value] ?: 0
        grandTotal += (denom.value.toLong() * count)
        if (denom.isCoin) {
            totalCoins += count
        } else {
            totalNotes += count
        }
    }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val formattedTotal = "Rs. ${currencyFormatter.format(grandTotal)}.00"

    fun generateSummaryText(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val sb = StringBuilder()
        sb.append("💵 දෛනික මුදල් ගණනය කිරීම් වාර්තාව (Cash Count Report)\n")
        sb.append("📅 දිනය: $dateStr\n")
        sb.append("───────────────────────\n")
        denominations.forEach { denom ->
            val count = countMap[denom.value] ?: 0
            if (count > 0) {
                val sub = denom.value.toLong() * count
                sb.append("${denom.labelSinhala} x $count = Rs. ${currencyFormatter.format(sub)}\n")
            }
        }
        sb.append("───────────────────────\n")
        sb.append("💰 මුළු එකතුව (Grand Total): $formattedTotal\n")
        sb.append("📝 නෝට්ටු ගණන: $totalNotes | කාසි ගණන: $totalCoins\n")
        sb.append("\nPolice Directory Daily Cash Tool")
        return sb.toString()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Grand Total Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Total",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "මුළු මුදල් එකතුව (Grand Total)",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                denominations.forEach { countMap[it.value] = 0 }
                                Toast.makeText(context, "සියලු අගයන් හිස් කරන ලදී", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formattedTotal,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "නෝට්ටු: $totalNotes • කාසි: $totalCoins",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                        Text(
                            text = convertAmountToSinhalaWords(grandTotal),
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val text = generateSummaryText()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Cash Report", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "වාර්තාව පිටපත් කරගන්නා ලදී (Copied)!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val text = generateSummaryText()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "දෛනික මුදල් වාර්තාව යවන්න")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / යවන්න", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section Title: Notes
        item {
            Text(
                text = "මුදල් නෝට්ටු (Currency Notes)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }

        items(denominations.filter { !it.isCoin }) { denom ->
            DenominationRowCard(
                denom = denom,
                count = countMap[denom.value] ?: 0,
                onCountChange = { countMap[denom.value] = it },
                formatter = currencyFormatter
            )
        }

        // Section Title: Coins
        item {
            Text(
                text = "කාසි වර්ග (Coins)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 10.dp, start = 2.dp)
            )
        }

        items(denominations.filter { it.isCoin }) { denom ->
            DenominationRowCard(
                denom = denom,
                count = countMap[denom.value] ?: 0,
                onCountChange = { countMap[denom.value] = it },
                formatter = currencyFormatter
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DenominationRowCard(
    denom: CashDenomination,
    count: Int,
    onCountChange: (Int) -> Unit,
    formatter: NumberFormat
) {
    val subtotal = denom.value.toLong() * count
    val hasCount = count > 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasCount) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (hasCount) denom.color.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Denomination Label & Subtotal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = denom.labelSinhala,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = denom.color
                )
                Text(
                    text = "= Rs. ${formatter.format(subtotal)}",
                    fontSize = 12.sp,
                    fontWeight = if (hasCount) FontWeight.Bold else FontWeight.Normal,
                    color = if (hasCount) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controls (- button, count input, + button)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (count > 0) onCountChange(count - 1) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = if (count == 0) "" else count.toString(),
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        val parsed = clean.toIntOrNull() ?: 0
                        onCountChange(parsed.coerceIn(0, 99999))
                    },
                    placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.width(68.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = denom.color,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { onCountChange(count + 1) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(denom.color.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = denom.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun convertAmountToSinhalaWords(amount: Long): String {
    if (amount <= 0) return "රුපියල් බිංදුවයි"
    return when {
        amount < 1000 -> "රුපියල් $amount යි"
        amount < 100000 -> {
            val thousands = amount / 1000
            val rem = amount % 1000
            if (rem == 0L) "රුපියල් දහස් $thousands යි" else "රුපියල් දහස් $thousands $rem යි"
        }
        else -> {
            val lakhs = amount / 100000
            val rem = amount % 100000
            if (rem == 0L) "රුපියල් ලක්ෂ $lakhs යි" else "රුපියල් ලක්ෂ $lakhs $rem යි"
        }
    }
}
