package com.flowtask.app.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.flowtask.app.BuildConfig
import com.flowtask.app.core.designsystem.components.OrbitBrandHeader
import com.flowtask.app.domain.model.ThemeMode
import com.flowtask.app.domain.model.UserPreferences

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onThemeChanged: (ThemeMode) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    on24HourChanged: (Boolean) -> Unit,
    onWeekStartChanged: (Boolean) -> Unit,
    onDefaultDurationChanged: (Int) -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenReplay: () -> Unit,
    exportText: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onNotificationsChanged(it)
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer -> writer.write(exportText) } }
    }
    val changeNotifications: (Boolean) -> Unit = { enabled ->
        val needsPermission = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else onNotificationsChanged(enabled)
    }

    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
    ) {
        item {
            OrbitBrandHeader(
                eyebrow = "Orbit settings",
                title = "Make Orbit yours.",
                subtitle = "Fine-tune the little details that keep you moving.",
            )
            SectionLabel("APPEARANCE")
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(Icons.Outlined.DarkMode, "Theme", preferences.themeMode.name.lowercase().replaceFirstChar(Char::uppercase), action = {
                        ThemeSelector(preferences.themeMode, onThemeChanged)
                    })
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.AccessTime, "24-hour time", "Use 18:00 instead of 6 PM", action = {
                        Switch(preferences.use24HourTime, onCheckedChange = on24HourChanged)
                    })
                }
            }
            SectionLabel("PLANNING")
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(Icons.Outlined.CalendarMonth, "Start of week", if (preferences.weekStartsOnMonday) "Monday" else "Sunday", onClick = { onWeekStartChanged(!preferences.weekStartsOnMonday) })
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.Update, "Default duration", "${preferences.defaultTaskDurationMinutes} minutes", onClick = {
                        onDefaultDurationChanged(when (preferences.defaultTaskDurationMinutes) { 25 -> 45; 45 -> 60; else -> 25 })
                    })
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.Notifications, "Reminders", if (preferences.notificationsEnabled) "10 minutes before" else "Off", action = {
                        Switch(preferences.notificationsEnabled, onCheckedChange = changeNotifications)
                    })
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.TouchApp, "Haptic feedback", "Subtle interaction cues", action = {
                        Switch(preferences.hapticsEnabled, onCheckedChange = onHapticsChanged)
                    })
                }
            }
            SectionLabel("ORBIT")
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(Icons.Outlined.Replay, "Routines", "Reusable sequences", onClick = onOpenRoutines)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.Replay, "Day Replay", "Review today", onClick = onOpenReplay)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.FileUpload, "Export data", "Create a local JSON archive", onClick = { exportLauncher.launch("orbit-export.json") })
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Outlined.Info, "About", "Orbit ${BuildConfig.VERSION_NAME}")
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    Row(Modifier.padding(start = 8.dp).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small).padding(2.dp)) {
        ThemeMode.entries.forEach { mode ->
            Text(
                mode.name.take(1),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected == mode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = "${mode.name.lowercase().replaceFirstChar(Char::uppercase)} theme" }.background(if (selected == mode) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent, MaterialTheme.shapes.extraSmall).clickable { onSelected(mode) }.padding(horizontal = 8.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
            Icon(icon, null, modifier = Modifier.padding(8.dp).size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
        if (action != null) action() else if (onClick != null) Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
