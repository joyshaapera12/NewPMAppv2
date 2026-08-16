package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun DualComparisonBar(
    title: String,
    homeValue: String,
    awayValue: String,
    homePercent: Float, // 0.0 to 1.0
    awayPercent: Float, // 0.0 to 1.0
    homeColor: Color = PitchMetricsTheme.colors.pitchGreen,
    awayColor: Color = PitchMetricsTheme.colors.secondaryBlue,
    modifier: Modifier = Modifier
) {
    val total = (homePercent + awayPercent).coerceAtLeast(0.01f)
    val normHome = (homePercent / total).coerceIn(0.05f, 0.95f)
    val normAway = (awayPercent / total).coerceIn(0.05f, 0.95f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = homeColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = PitchMetricsTheme.colors.textMuted
            )
            Text(
                text = awayValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = awayColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(PitchMetricsTheme.colors.border.copy(alpha = 0.3f)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(normHome)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                    .background(homeColor)
            )
            Box(
                modifier = Modifier
                    .weight(normAway)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                    .background(awayColor)
            )
        }
    }
}
