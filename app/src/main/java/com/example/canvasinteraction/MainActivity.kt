package com.example.canvasinteraction

import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.canvasinteraction.ui.theme.CanvasInteractionTheme
import kotlin.math.*

class MainActivity : ComponentActivity() {
    val clock = ClockGraphics()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanvasInteractionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Canvas(
                        modifier = Modifier/*.pointerInput(key1 = Unit) {
                        detectTapGestures {
                            if (offsetWithinCircle(it, circle.center, circle.radius))
                                println("circle was clicked: $it")
                        }
                    }*/, onDraw = onDraw
                    )

/*
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
*/
                }
            }
        }
    }

    private val onDraw: DrawScope.() -> Unit = {
        clock.center = Offset(
            size.width / 2f, size.height / 2f
        )
        clock.radius = size.minDimension / 3f
        /*drawCircle(
            color = Color.Green, radius = circle.radius, center = circle.center
        )*/

        clock.innerEndStickCircleRadius = clock.radius + 60
        drawCircle(
            color = Color.LightGray,
            radius = clock.radius,
            center = clock.center
        )
        clock.outerEndStickCircleRadius = clock.radius + 90
        clock.numberCircleRadius = clock.radius + 30
        var strokeWidth = 0f
        for (i in 0..29) { // draw sticks
            strokeWidth = if (i.mod(5) == 0) 8f else 2f

            drawLine(
                color = Color.Magenta,
                start = Offset(
                    clock.center.x - (cos(clock.oneSecondOrMinuteRadian * i) * clock.innerEndStickCircleRadius),
                    clock.center.y + (sin(clock.oneSecondOrMinuteRadian * i) * clock.innerEndStickCircleRadius)
                ),
                end = Offset(
                    clock.center.x - (cos(clock.oneSecondOrMinuteRadian * i) * clock.outerEndStickCircleRadius),
                    clock.center.y + (sin(clock.oneSecondOrMinuteRadian * i) * clock.outerEndStickCircleRadius)
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color.Magenta,
                start = Offset(
                    clock.center.x + (cos(clock.oneSecondOrMinuteRadian * i) * clock.innerEndStickCircleRadius),
                    clock.center.y - (sin(clock.oneSecondOrMinuteRadian * i) * clock.innerEndStickCircleRadius)
                ),
                end = Offset(
                    clock.center.x + (cos(clock.oneSecondOrMinuteRadian * i) * clock.outerEndStickCircleRadius),
                    clock.center.y - (sin(clock.oneSecondOrMinuteRadian * i) * clock.outerEndStickCircleRadius)
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        val paint = android.graphics.Paint().apply {
            textSize = 60f
            color = Color.Red.toArgb()
        }

        var currentRadian = Math.toRadians(90.0)
        var textX = 0f
        var textY = 0f
        val rect = Rect()
        val hour12 = 12
        var relativeNumberBaseline = 0f
        for (i in hour12 downTo 7) { // draw numbers
            relativeNumberBaseline = (hour12 - i) / 6f
            paint.getTextBounds("$i", 0, "$i".length, rect)
            /*textX =
                clock.center.x + cos(currentRadian).toFloat() * clock.innerEndStickCircleRadius
            textY =
                clock.center.y + sin(currentRadian).toFloat() * clock.innerEndStickCircleRadius - rect.height() / 2*/
            if (cos(currentRadian) in 0f..0.000001f) // since we are going counterclockwise from 12 to 7,
            // the only hour when cos equals 0 is 12, which is the first loop
            {
                textX =
                    clock.center.x - rect.width() / 2 + rect.left
                textY =
                    clock.center.y - sin(currentRadian).toFloat() * clock.innerEndStickCircleRadius - rect.top * relativeNumberBaseline
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "$i",
                        textX,
                        textY,
                        paint
                    )
                }

                paint.getTextBounds("${i - 6}", 0, "${i - 6}".length, rect)
                textY =
                    clock.center.y + sin(currentRadian).toFloat() * clock.innerEndStickCircleRadius + rect.top * relativeNumberBaseline
                drawIntoCanvas {
                    it.nativeCanvas.drawText( // then we draw the opposite number
                        "${i - 6}",
                        textX,
                        textY,
                        paint
                    )
                }
            } else {
                textX =
                    clock.center.x + cos(currentRadian).toFloat() * clock.innerEndStickCircleRadius
                textY =
                    clock.center.y - sin(currentRadian).toFloat() * clock.innerEndStickCircleRadius + rect.top * relativeNumberBaseline
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "$i",
                        textX,
                        textY,
                        paint
                    )
                }

                paint.getTextBounds("${i - 6}", 0, "${i - 6}".length, rect)
                textX =
                    clock.center.x - cos(currentRadian).toFloat() * clock.innerEndStickCircleRadius - rect.width()
                textY =
                    clock.center.y + sin(currentRadian).toFloat() * clock.innerEndStickCircleRadius + rect.top * relativeNumberBaseline
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "${i - 6}",
                        textX,
                        textY,
                        paint
                    )
                }
            }

            currentRadian += clock.oneSecondOrMinuteRadian * 5
        }
    }
}

fun DrawScope.foo() {}
private fun offsetWithinCircle(offset: Offset, centerOffset: Offset, radius: Float): Boolean =
    offset.x in centerOffset.x - radius..centerOffset.x + radius
            &&
            abs(offset.y - centerOffset.y) / radius <= sqrt(
        1 - (abs(offset.x - centerOffset.x) / radius).pow(
            2
        )
    )

class ClockGraphics(var center: Offset = Offset(0f, 0f), var radius: Float = 0f) {
    var outerEndStickCircleRadius = 0f
    var innerEndStickCircleRadius = 0f
    var numberCircleRadius = 0f

    private val oneSecondOrMinuteInterval = 360.0 / 60
    val oneSecondOrMinuteRadian = Math.toRadians(oneSecondOrMinuteInterval).toFloat()
}