package com.flowtask.app.presentation.taskmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.flowtask.app.BuildConfig

@Composable
fun ProfileScreen(
    notificationsEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onShowOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onNotificationsChanged(granted)
    }
    val changeNotifications: (Boolean) -> Unit = { enabled ->
        val needsPermission = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onNotificationsChanged(enabled)
        }
    }

    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp, 24.dp, 20.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item {
            SoftCard {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(58.dp).background(AppPrimary.copy(alpha = .14f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("LV", color = AppPrimary, fontWeight = FontWeight.Bold) }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Livia Vaccaro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Product Designer", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            SettingRow(
                Icons.Outlined.Notifications,
                "Notifications",
                if (notificationsEnabled) "Remind me when tasks are due" else "Task reminders are off",
            ) {
                Switch(checked = notificationsEnabled, onCheckedChange = changeNotifications)
            }
        }
        item { SettingRow(Icons.Outlined.DarkMode, "Appearance", "Follows your device theme") }
        item { SettingRow(Icons.Outlined.Info, "About", "Task Management ${BuildConfig.VERSION_NAME}") }
        item {
            SoftCard {
                TextButton(onClick = onShowOnboarding, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Outlined.Refresh, null)
                    Text("Show onboarding again", Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: @Composable (() -> Unit)? = null,
) {
    SoftCard {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(AppPrimary.copy(alpha = .1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = AppPrimary) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            action?.invoke()
        }
    }
}
