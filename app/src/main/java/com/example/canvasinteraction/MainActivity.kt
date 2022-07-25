package com.example.canvasinteraction

import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.canvasinteraction.ui.theme.CanvasInteractionTheme
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private val clockGraphics = ClockGraphics()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanvasInteractionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ClockCanvas(modifier = Modifier, clockGraphics = clockGraphics)
                }
            }
        }
    }
}

@Composable
fun TextBoundsExperiment() {
    Canvas(modifier = Modifier) {
        drawIntoCanvas {
            val text = "9"
            val paint = android.graphics.Paint().apply {
                textSize = 60f
                color = Color.Red.toArgb()
            }
            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            var baseline = 10 - rect.top.toFloat()
            it.nativeCanvas.drawText(text, 5.dp.toPx(), baseline, paint)
            drawLine(
                Color.Blue,
                Offset(
                    5.dp.toPx() + rect.left,
                    baseline + rect.top
                ),
                Offset(
                    5.dp.toPx() + rect.left + rect.width(),
                    baseline + rect.top
                )
            )
            drawLine(
                Color.Blue,
                Offset(
                    5.dp.toPx() + rect.left,
                    baseline + rect.bottom.toFloat()
                ),
                Offset(
                    5.dp.toPx() + rect.left + rect.width(),
                    baseline + rect.bottom.toFloat()
                )
            )

            baseline = 10 - rect.top.toFloat() * .5f
            it.nativeCanvas.drawText(text, 25.dp.toPx(), baseline, paint)

            baseline = 10 - rect.top.toFloat() * (5 / 6f)
            it.nativeCanvas.drawText(text, 45.dp.toPx(), baseline, paint)
        }
    }
}

private fun offsetWithinCircle(offset: Offset, centerOffset: Offset, radius: Float): Boolean =
    offset.x in centerOffset.x - radius..centerOffset.x + radius
            &&
            abs(offset.y - centerOffset.y) / radius <= sqrt(
        1 - (abs(offset.x - centerOffset.x) / radius).pow(
            2
        )
    )