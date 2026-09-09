package com.afomsteam.enlistedplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afomsteam.enlistedplanner.data.BrainSignal
import com.afomsteam.enlistedplanner.data.Severity
import java.time.LocalDate

@Composable
fun SectionTitle(title: String, subtitle: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoftBlue, letterSpacing = 0.7.sp)
            if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = TextMuted)
        }
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun PlannerCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val cardModifier = if (onClick != null) modifier.fillMaxWidth().clickable { onClick() } else modifier.fillMaxWidth()
    Card(cardModifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardBlue.copy(alpha = 0.78f))) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SignalCard(signal: BrainSignal, onClick: (() -> Unit)? = null) {
    val color = when (signal.severity) {
        Severity.CRITICAL -> Critical
        Severity.WARNING -> Warn
        Severity.INFO -> AirBlue
        Severity.GOOD -> Good
    }
    val icon = when (signal.severity) {
        Severity.CRITICAL -> Icons.Default.Error
        Severity.WARNING -> Icons.Default.Warning
        Severity.INFO -> Icons.Default.Info
        Severity.GOOD -> Icons.Default.CheckCircle
    }
    PlannerCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(signal.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (signal.detail.isNotBlank()) Text(signal.detail, fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 3.dp))
                if (signal.category.isNotBlank()) Text(signal.category.uppercase(), fontSize = 10.sp, color = color, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
fun StatusChip(text: String, severity: Severity = Severity.INFO) {
    val color = when (severity) {
        Severity.CRITICAL -> Critical
        Severity.WARNING -> Warn
        Severity.INFO -> AirBlue
        Severity.GOOD -> Good
    }
    Surface(color = color.copy(alpha = 0.17f), shape = RoundedCornerShape(50)) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}

@Composable
fun EmptyState(title: String, body: String) {
    PlannerCard {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(body, color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun LabeledField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "", singleLine: Boolean = true, modifier: Modifier = Modifier.fillMaxWidth()) {
    val context = LocalContext.current
    val lower = label.lowercase()
    val dateLike = lower.contains("date") || lower.contains("scod") || lower.contains("pfra") ||
        lower.contains("expiration") || lower.contains("suspense") || lower.contains("feedback") ||
        lower.contains("supervision start") || lower.contains("completion") || lower == "start" || lower == "end"
    fun openCalendar() {
        val initial = runCatching { LocalDate.parse(value) }.getOrElse { LocalDate.now() }
        android.app.DatePickerDialog(
            context,
            { _, year, month, day -> onValueChange(LocalDate.of(year, month + 1, day).toString()) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).show()
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        singleLine = singleLine,
        trailingIcon = if (dateLike) ({ TextButton(onClick = { openCalendar() }) { Text("📅") } }) else null,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AirBlue,
            unfocusedBorderColor = TextMuted.copy(alpha = .45f)
        )
    )
}

@Composable
fun <T> DropdownField(label: String, selected: T, options: List<T>, display: (T) -> String, onSelected: (T) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text(label.uppercase(), fontSize = 10.sp, color = TextMuted)
                Text(display(selected), color = Color.White)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(display(option)) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 21.sp)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Text(subtitle, fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
}
