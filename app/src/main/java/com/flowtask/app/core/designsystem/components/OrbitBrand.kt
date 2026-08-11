package com.flowtask.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.theme.OrbitAqua
import com.flowtask.app.core.designsystem.theme.OrbitBerry
import com.flowtask.app.core.designsystem.theme.OrbitLilac
import com.flowtask.app.core.designsystem.theme.OrbitRose

/** A small, ownable visual signature used by Orbit's major destinations. */
@Composable
fun OrbitBrandHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            androidx.compose.foundation.Canvas(
                Modifier
                    .matchParentSize()
                    .padding(start = 128.dp),
            ) {
                drawOrbitArcs()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text(
                    eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 4.dp, end = 76.dp),
                )
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 5.dp, end = 62.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun OrbitGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (enabled) {
                        Brush.horizontalGradient(listOf(OrbitBerry, OrbitRose))
                    } else {
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant),
                        )
                    },
                    MaterialTheme.shapes.large,
                )
                .padding(horizontal = 20.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

private fun DrawScope.drawOrbitArcs() {
    val outerRadius = 70.dp.toPx()
    val middleRadius = 49.dp.toPx()
    val innerRadius = 28.dp.toPx()
    drawCircle(OrbitAqua.copy(alpha = .92f), outerRadius, center = androidx.compose.ui.geometry.Offset(size.width + 17.dp.toPx(), 4.dp.toPx()))
    drawCircle(OrbitBerry.copy(alpha = .92f), middleRadius, center = androidx.compose.ui.geometry.Offset(size.width + 11.dp.toPx(), 4.dp.toPx()))
    drawCircle(OrbitLilac.copy(alpha = .9f), innerRadius, center = androidx.compose.ui.geometry.Offset(size.width + 8.dp.toPx(), 5.dp.toPx()))
}
