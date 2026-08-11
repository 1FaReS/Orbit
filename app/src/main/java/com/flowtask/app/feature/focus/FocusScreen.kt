package com.flowtask.app.feature.focus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowtask.app.core.designsystem.components.OrbitGradientButton
import com.flowtask.app.core.designsystem.theme.OrbitAqua
import com.flowtask.app.core.designsystem.theme.OrbitLilac
import com.flowtask.app.domain.model.FocusSession
import com.flowtask.app.domain.model.FocusSessionStatus
import com.flowtask.app.domain.model.Task
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    session: FocusSession?,
    task: Task?,
    onPauseChanged: (Boolean) -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
) {
    if (session == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No active focus session", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onBack) { Text("Return to today") }
            }
        }
        return
    }
    BackHandler(onBack = onBack)
    val totalSeconds = session.durationMinutes * 60
    var remainingSeconds by remember(session.id) { mutableIntStateOf(totalSeconds) }
    val isPaused = session.status == FocusSessionStatus.PAUSED
    LaunchedEffect(session.id, isPaused) {
        while (!isPaused && remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds--
        }
        if (remainingSeconds == 0) onFinish()
    }
    val progress by animateFloatAsState(remainingSeconds / totalSeconds.toFloat(), label = "focus-progress")
    val background by animateColorAsState(
        if (isPaused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
        label = "focus-background",
    )

    Box(Modifier.fillMaxSize().background(background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(OrbitAqua.copy(alpha = .10f), radius = 150.dp.toPx(), center = Offset(size.width + 52.dp.toPx(), 68.dp.toPx()))
            drawCircle(OrbitLilac.copy(alpha = .14f), radius = 125.dp.toPx(), center = Offset(-46.dp.toPx(), size.height - 24.dp.toPx()))
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ORBIT FOCUS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onBack) { Icon(Icons.Outlined.Close, "Close focus view") }
        }
        Spacer(Modifier.weight(.75f))
        Text(task?.category?.uppercase() ?: "DEEP WORK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            task?.title ?: "Focus session",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 10.dp),
        )
        Box(Modifier.padding(top = 40.dp).size(232.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(12.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .11f),
                strokeWidth = 6.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatTimer(remainingSeconds), style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp), fontWeight = FontWeight.Medium)
                Text(if (isPaused) "Paused" else "Remaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Spacer(Modifier.height(48.dp))
        OrbitGradientButton(onClick = { onPauseChanged(!isPaused) }, modifier = Modifier.fillMaxWidth(.72f)) {
            Icon(if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, null)
            Text(if (isPaused) "Resume" else "Pause", Modifier.padding(start = 8.dp))
        }
        OutlinedButton(onClick = onFinish, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth(.72f).padding(top = 10.dp)) { Text("Finish session") }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onCancel) { Text("Cancel session", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private fun formatTimer(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
