package com.example.canvasinteraction

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

class ClockGraphics(var center: Offset = Offset(0f, 0f), var radius: Float = 0f) {
    private val oneSecondRadian = Math.toRadians(360.0 / 60)
    private val oneMinuteRadian = oneSecondRadian
    private val oneHourRadian = Math.toRadians(360.0 / 12)
    private var stickColor = Color.Black
    var numberColor = Color.Black
    private var smallStroke = 2f
    private var bigStroke = 8f
    private var strokeCap = StrokeCap.Round
    private val numberPaint = Paint().apply {
        textSize = 60f
        color = Color.Red.toArgb()
        isAntiAlias = true
        isDither = true
    }
    private val casePaint = Paint().apply {
        color = Color.Magenta.toArgb()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
        isDither = true
    }
    private val numberBounds = Rect()
    private val spotOf12 = Math.toRadians(90.0)

    fun draw(drawScope: DrawScope, time: LocalTime) {
        center = Offset(drawScope.size.width / 2f, drawScope.size.height / 2f)
        radius = drawScope.size.minDimension / 2.2f
        val outerEndStickCircleRadius = radius - 10
        val innerEndStickCircleRadius = outerEndStickCircleRadius - 30
        val numberCircleRadius = innerEndStickCircleRadius - 15
        val secHandLength = numberCircleRadius - 100
        val minHandLength = numberCircleRadius - 140
        val hourHandLength = numberCircleRadius - 245

        drawScope.drawIntoCanvas {
            it.nativeCanvas.drawCircle(center.x, center.y, radius, casePaint)
        }

        var numberMarker = spotOf12

        for (i in 0..29) { // draw sticks
            val isFactorOf5 = i.mod(5) == 0
            val stickWidth = if (isFactorOf5) bigStroke else smallStroke
            val extraLength = if (isFactorOf5) 0f else 10f
            drawScope.drawSticks(
                Offset(
                    center.x - (cos(oneSecondRadian * i).toFloat() * (innerEndStickCircleRadius + extraLength)),
                    center.y + (sin(oneSecondRadian * i).toFloat() * (innerEndStickCircleRadius + extraLength))
                ),
                Offset(
                    center.x - (cos(oneSecondRadian * i).toFloat() * outerEndStickCircleRadius),
                    center.y + (sin(oneSecondRadian * i).toFloat() * outerEndStickCircleRadius)
                ),
                stickWidth,
            )

            drawScope.drawSticks(
                Offset(
                    center.x + (cos(oneSecondRadian * i).toFloat() * (innerEndStickCircleRadius + extraLength)),
                    center.y - (sin(oneSecondRadian * i).toFloat() * (innerEndStickCircleRadius + extraLength))
                ),
                Offset(
                    center.x + (cos(oneSecondRadian * i).toFloat() * outerEndStickCircleRadius),
                    center.y - (sin(oneSecondRadian * i).toFloat() * outerEndStickCircleRadius)
                ),
                stickWidth,
            )
        }

        val fullClock = 12
        val halfClock = fullClock / 2

        for (i in fullClock downTo 7) { // draw numbers
            var number = "$i"
            val relativeNumberBaseline = (fullClock - i) / halfClock.toFloat()
            numberPaint.getTextBounds(number, 0, number.length, numberBounds)
            if (cos(numberMarker) in 0f..0.000001f) // since we are going counterclockwise from 12 to 7,
            // the only hour when cos equals 0 is 12, which is the first loop
            {
                var numberXCoordinate =
                    center.x - (numberPaint.measureText(number)) / 2
                var numberYCoordinate =
                    center.y - sin(numberMarker).toFloat() * numberCircleRadius - numberBounds.top * (1 - relativeNumberBaseline)
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        numberXCoordinate,
                        numberYCoordinate,
                        numberPaint
                    )
                }

                number = "${i - 6}"
                numberPaint.getTextBounds(number, 0, number.length, numberBounds)
                numberXCoordinate =
                    center.x - (numberPaint.measureText(number)) / 2
                numberYCoordinate =
                    center.y + sin(numberMarker).toFloat() * numberCircleRadius - numberBounds.top * relativeNumberBaseline
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText( // then we draw the opposite number
                        number,
                        numberXCoordinate,
                        numberYCoordinate,
                        numberPaint
                    )
                }
            } else {
                var numberXCoordinate =
                    center.x + cos(numberMarker).toFloat() * numberCircleRadius
                var numberYCoordinate =
                    center.y - sin(numberMarker).toFloat() * numberCircleRadius - numberBounds.top * (1 - relativeNumberBaseline)
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        numberXCoordinate,
                        numberYCoordinate,
                        numberPaint
                    )
                }
                number = "${i - 6}"
                numberPaint.getTextBounds(number, 0, number.length, numberBounds)
                numberXCoordinate =
                    center.x - cos(numberMarker).toFloat() * numberCircleRadius - numberPaint.measureText(
                        number
                    )
                numberYCoordinate =
                    center.y + sin(numberMarker).toFloat() * numberCircleRadius - numberBounds.top * relativeNumberBaseline
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        numberXCoordinate,
                        numberYCoordinate,
                        numberPaint
                    )
                }
            }

            numberMarker += oneSecondRadian * 5
        }

        val handTailLength = 10.0f
        val secondHandWidth = 3.dp.value
        val minuteHandWidth = 8.dp.value
        val hourHandWidth = 15.dp.value
        val handColor = Color.Black

        val secondRadian = spotOf12 - time.second * oneSecondRadian
        drawScope.drawLine(
            // second hand
            color = handColor, start = Offset(center.x, center.y),
            end = Offset(
                center.x + cos(secondRadian).toFloat() * secHandLength,
                center.y - sin(secondRadian).toFloat() * secHandLength
            ),
            strokeWidth = secondHandWidth,
        )
        drawScope.drawLine(
            // second hand tail
            color = handColor, start = Offset(center.x, center.y),
            end = Offset(
                center.x - cos(secondRadian).toFloat() * handTailLength,
                center.y + sin(secondRadian).toFloat() * handTailLength
            ),
            strokeWidth = secondHandWidth,
        )

        val minRadian = spotOf12 - ((time.minute /*+ (time.second / 60f)*/) * oneMinuteRadian)
        drawScope.drawLine(
            // minute hand
            color = handColor, start = Offset(center.x, center.y),
            end = Offset(
                center.x + cos(minRadian).toFloat() * minHandLength,
                center.y - sin(minRadian).toFloat() * minHandLength
            ),
            strokeWidth = minuteHandWidth,
        )
        drawScope.drawLine(
            // minute hand tail
            color = handColor, start = Offset(center.x, center.y),
            end = Offset(
                center.x - cos(minRadian).toFloat() * handTailLength,
                center.y + sin(minRadian).toFloat() * handTailLength
            ),
            strokeWidth = minuteHandWidth,
        )

        val hourRadian = spotOf12 - time.hour * oneHourRadian
        drawScope.drawLine(
            color = handColor, start = Offset(center.x, center.y),
            end = Offset(
                center.x + cos(hourRadian).toFloat() * hourHandLength,
                center.y - sin(hourRadian).toFloat() * hourHandLength
            ),
            strokeWidth = hourHandWidth,
            cap = StrokeCap.Round
        )
        drawScope.drawLine(
            color = handColor, start = Offset(center.x, center.y),
            end = Offset(
                center.x - cos(hourRadian).toFloat() * handTailLength,
                center.y + sin(hourRadian).toFloat() * handTailLength
            ),
            strokeWidth = hourHandWidth,
            cap = StrokeCap.Round
        )

        drawScope.drawCircle(color = Color.Gray, radius = 3f, center = center)
    }

    private fun DrawScope.drawSticks(start: Offset, end: Offset, strokeWidth: Float) {
        drawLine(
            color = stickColor,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = strokeCap
        )
    }
}

@Composable
fun ClockCanvas(modifier: Modifier, clockGraphics: ClockGraphics) {
    var time by remember {
        mutableStateOf(LocalTime.now())
    }
    LaunchedEffect(key1 = Unit) {
        launch {
            while (true) {
                delay(999)
                time = LocalTime.now()
            }
        }
    }
    Canvas(modifier = modifier, onDraw = { clockGraphics.draw(this, time) })
}