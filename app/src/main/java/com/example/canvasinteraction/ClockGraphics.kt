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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

class ClockGraphics(var center: Offset = Offset(0f, 0f), var caseRadius: Float = 0f) {
    private var outerEndStickCircleRadius = 0f
    private val oneSecondOrMinuteRadian = Math.toRadians(360.0 / 60).toFloat()
    private var stickColor = Color.Black
    var numberColor = Color.Black
    private var smallStroke = 2f
    private var bigStroke = 8f
    private var strokeCap = StrokeCap.Round
    private val numberPaint = android.graphics.Paint().apply {
        textSize = 60f
        color = Color.Red.toArgb()
        isAntiAlias = true
        isDither = true
    }
    private val casePaint = android.graphics.Paint().apply {
        color = Color.Magenta.toArgb()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
        isDither = true
    }
    private var currentRadian = Math.toRadians(90.0)
    private val numberBounds = Rect()

    fun draw(drawScope: DrawScope, second: Int) {
        center = Offset(drawScope.size.width / 2f, drawScope.size.height / 2f)
        caseRadius = drawScope.size.minDimension / 2.2f
        outerEndStickCircleRadius = caseRadius - 10
        var innerEndStickCircleRadius = outerEndStickCircleRadius - 30
        var numberCircleRadius = innerEndStickCircleRadius - 15
        var secHandLength = numberCircleRadius - 100

        drawScope.drawIntoCanvas {
            it.nativeCanvas.drawCircle(center.x, center.y, caseRadius, casePaint)
        }

        for (i in 0..29) { // draw sticks
            var isFactorOf5 = i.mod(5) == 0
            var stickWidth = if (isFactorOf5) bigStroke else smallStroke
            val extraLength = if (isFactorOf5) 0f else 10f
            drawScope.drawSticks(
                Offset(
                    center.x - (cos(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength)),
                    center.y + (sin(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength))
                ),
                Offset(
                    center.x - (cos(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius),
                    center.y + (sin(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius)
                ),
                stickWidth,
            )

            drawScope.drawSticks(
                Offset(
                    center.x + (cos(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength)),
                    center.y - (sin(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength))
                ),
                Offset(
                    center.x + (cos(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius),
                    center.y - (sin(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius)
                ),
                stickWidth,
            )
        }

        val fullClock = 12
        val halfClock = fullClock / 2
        var numberXCoordinate = 0f
        var numberYCoordinate = 0f
        for (i in fullClock downTo 7) { // draw numbers
            var number = "$i"
            var relativeNumberBaseline = (fullClock - i) / halfClock.toFloat()
            numberPaint.getTextBounds(number, 0, number.length, numberBounds)

            if (cos(currentRadian) in 0f..0.000001f) // since we are going counterclockwise from 12 to 7,
            // the only hour when cos equals 0 is 12, which is the first loop
            {
                numberXCoordinate =
                    center.x - (numberPaint.measureText(number)) / 2
                numberYCoordinate =
                    center.y - sin(currentRadian).toFloat() * numberCircleRadius - numberBounds.top * (1 - relativeNumberBaseline)
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
                    center.y + sin(currentRadian).toFloat() * numberCircleRadius - numberBounds.top * relativeNumberBaseline
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText( // then we draw the opposite number
                        number,
                        numberXCoordinate,
                        numberYCoordinate,
                        numberPaint
                    )
                }
            } else {
                numberXCoordinate =
                    center.x + cos(currentRadian).toFloat() * numberCircleRadius
                numberYCoordinate =
                    center.y - sin(currentRadian).toFloat() * numberCircleRadius - numberBounds.top * (1 - relativeNumberBaseline)
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
                    center.x - cos(currentRadian).toFloat() * numberCircleRadius - numberPaint.measureText(
                        number
                    )
                numberYCoordinate =
                    center.y + sin(currentRadian).toFloat() * numberCircleRadius - numberBounds.top * relativeNumberBaseline
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        number,
                        numberXCoordinate,
                        numberYCoordinate,
                        numberPaint
                    )
                }
            }

            currentRadian += oneSecondOrMinuteRadian * 5
        }

        drawScope.drawLine(Color.Black, Offset(center.x, center.y),Offset())
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
    var sec by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(key1 = Unit) {
        launch {
            sec = LocalTime.now().second
            delay(2)
        }
    }
    Canvas(modifier = modifier, onDraw = { clockGraphics.draw(this, sec) })
}

/*
fun DrawScope.drawClock(clockGraphics: ClockGraphics) {
    center = Offset(drawScope.size.width / 2f, drawScope.size.height / 2f)
    caseRadius = drawScope.size.minDimension / 2.2f
    outerEndStickCircleRadius = caseRadius - 10
    innerEndStickCircleRadius = caseRadius - 40
    numberCircleRadius = caseRadius - 50

    drawCircle(
        color = Color.LightGray,
        radius = caseRadius,
        center = center
    )

    var strokeWidth = 0f

    for (i in 0..29) { // draw sticks
        strokeWidth = if (i.mod(5) == 0) bigStroke else smallStroke
        drawLine(
            color = stickColor,
            start = Offset(
                center.x - (cos(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength)),
                center.y + (sin(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength))
            ),
            end = Offset(
                center.x - (cos(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius),
                center.y + (sin(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius)
            ),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = stickColor,
            start = Offset(
                center.x + (cos(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength)),
                center.y - (sin(oneSecondOrMinuteRadian * i) * (innerEndStickCircleRadius + extraLength))
            ),
            end = Offset(
                center.x + (cos(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius),
                center.y - (sin(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius)
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

    var relativeNumberBaseline = 0f
    for (i in fullClock downTo 7) { // draw numbers
        relativeNumberBaseline = (fullClock - i) / halfClock.toFloat()
        paint.getTextBounds("$i", 0, "$i".length, rect)

        if (cos(currentRadian) in 0f..0.000001f) // since we are going counterclockwise from 12 to 7,
        // the only hour when cos equals 0 is 12, which is the first loop
        {
            textX =
                center.x - rect.width() / 2 + rect.left
            textY =
                center.y - sin(currentRadian).toFloat() * (innerEndStickCircleRadius + extraLength) - rect.top * (1 - relativeNumberBaseline)
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
                center.y + sin(currentRadian).toFloat() * (innerEndStickCircleRadius + extraLength) - rect.top * relativeNumberBaseline
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
                center.x + cos(currentRadian).toFloat() * (innerEndStickCircleRadius + extraLength)
            textY =
                center.y - sin(currentRadian).toFloat() * (innerEndStickCircleRadius + extraLength) - rect.top * (1 - relativeNumberBaseline)
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
                center.x - cos(currentRadian).toFloat() * (innerEndStickCircleRadius + extraLength) - rect.width()
            textY =
                center.y + sin(currentRadian).toFloat() * (innerEndStickCircleRadius + extraLength) - rect.top * relativeNumberBaseline
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    "${i - 6}",
                    textX,
                    textY,
                    paint
                )
            }
        }

        currentRadian += oneSecondOrMinuteRadian * 5
    }
}*/
