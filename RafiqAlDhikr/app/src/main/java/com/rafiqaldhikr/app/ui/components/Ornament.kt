package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Outline of an eight-pointed star (two squares, one turned 45 degrees) centred on [c] with half-diagonal [r]. */
private fun DrawScope.eightPointedStar(c: Offset, r: Float, color: Color, strokeWidth: Float) {
    val k = r * 0.7071f // half-side of an axis-aligned square whose corners touch the circle of radius r
    val square = Path().apply {
        moveTo(c.x - k, c.y - k); lineTo(c.x + k, c.y - k); lineTo(c.x + k, c.y + k); lineTo(c.x - k, c.y + k); close()
    }
    val diamond = Path().apply {
        moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close()
    }
    val stroke = Stroke(width = strokeWidth)
    drawPath(square, color, style = stroke)
    drawPath(diamond, color, style = stroke)
}

/** Draws a faint tiled geometric (eight-pointed star) pattern behind the content. */
fun Modifier.islamicPattern(color: Color, cell: Dp = 40.dp, strokeWidth: Dp = 0.8.dp): Modifier =
    this.clipToBounds().drawBehind {
        val step = cell.toPx()
        val r = step * 0.38f
        val sw = strokeWidth.toPx()
        var y = step / 2
        var row = 0
        while (y < size.height + step) {
            // Alternate rows are shifted by half a cell for a brick-like repeat.
            var x = if (row % 2 == 0) step / 2 else 0f
            while (x < size.width + step) {
                eightPointedStar(Offset(x, y), r, color, sw)
                x += step
            }
            y += step * 0.75f
            row++
        }
    }

/** A thin line with a small star in the middle, used under screen titles. */
@Composable
fun OrnamentDivider(modifier: Modifier = Modifier) {
    val gold = MaterialTheme.colorScheme.secondary
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(gold.copy(alpha = 0.55f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
        }
        Canvas(modifier = Modifier.size(12.dp)) {
            eightPointedStar(Offset(size.width / 2, size.height / 2), size.width / 2, gold, 1.2.dp.toPx())
        }
        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(gold.copy(alpha = 0.55f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
        }
    }
}
