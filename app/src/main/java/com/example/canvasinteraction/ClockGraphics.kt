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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ClockGraphics(var center: Offset = Offset(0f, 0f), var radius: Float = 0f) {
    private val oneSecondRadian = (2 * PI) / 60f
    private val oneMinuteRadian = oneSecondRadian
    private val oneHourRadian = (2 * PI) / 12f
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
    private val spotOf12 = PI / 2f
    private val handTailLength = 10.0f
    private val secondHandWidth = 3.dp.value
    private val minuteHandWidth = 8.dp.value
    private val hourHandWidth = 15.dp.value
    var handColor = Color.Black
    private val fullClock = 12
    private val halfClock = fullClock / 2

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
            val innerRelativeX =
                cos(oneSecondRadian * i).toFloat() * (innerEndStickCircleRadius + extraLength)
            val innerRelativeY =
                sin(oneSecondRadian * i).toFloat() * (innerEndStickCircleRadius + extraLength)
            val outerRelativeX = cos(oneSecondRadian * i).toFloat() * outerEndStickCircleRadius
            val outerRelativeY = sin(oneSecondRadian * i).toFloat() * outerEndStickCircleRadius
            drawScope.drawSticks(
                Offset(
                    center.x - innerRelativeX,
                    center.y + innerRelativeY
                ),
                Offset(
                    center.x - outerRelativeX,
                    center.y + outerRelativeY
                ),
                stickWidth,
            )

            drawScope.drawSticks(
                Offset(
                    center.x + innerRelativeX,
                    center.y - innerRelativeY
                ),
                Offset(
                    center.x + outerRelativeX,
                    center.y - outerRelativeY
                ),
                stickWidth,
            )
        }

        for (i in fullClock downTo 7) { // draw numbers
            var number = "$i"
            val relativeNumberBaseline = (fullClock - i) / halfClock.toFloat()
            numberPaint.getTextBounds(number, 0, number.length, numberBounds)
            val relativeXOfNumberMarker = cos(numberMarker).toFloat() * numberCircleRadius
            val relativeYOfNumberMarker = sin(numberMarker).toFloat() * numberCircleRadius
            if (cos(numberMarker) in 0f..0.000001f) // since we are going counterclockwise from 12 to 7,
            // the only hour when cos equals 0 is 12, which is the first loop
            {
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        center.x - (numberPaint.measureText(number)) / 2,
                        center.y - relativeYOfNumberMarker - numberBounds.top * (1 - relativeNumberBaseline),
                        numberPaint
                    )
                }

                number = "${i - 6}"
                numberPaint.getTextBounds(number, 0, number.length, numberBounds)
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText( // then we draw the opposite number
                        number,
                        center.x - (numberPaint.measureText(number)) / 2,
                        center.y + relativeYOfNumberMarker - numberBounds.top * relativeNumberBaseline,
                        numberPaint
                    )
                }
            } else {
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        center.x + relativeXOfNumberMarker,
                        center.y - relativeYOfNumberMarker - numberBounds.top * (1 - relativeNumberBaseline),
                        numberPaint
                    )
                }
                number = "${i - 6}"
                numberPaint.getTextBounds(number, 0, number.length, numberBounds)
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        center.x - relativeXOfNumberMarker - numberPaint.measureText(number),
                        center.y + relativeYOfNumberMarker - numberBounds.top * relativeNumberBaseline,
                        numberPaint
                    )
                }
            }

            numberMarker += oneSecondRadian * 5
        }

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