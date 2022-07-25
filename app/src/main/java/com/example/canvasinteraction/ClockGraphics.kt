package com.example.canvasinteraction

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.cos
import kotlin.math.sin

class ClockGraphics(var center: Offset = Offset(0f, 0f), var caseRadius: Float = 0f) {
    var outerEndStickCircleRadius = 0f
    private var innerEndStickCircleRadius = 0f
    private var numberCircleRadius = 0f
    private val fullClock = 12
    private val halfClock = 6
    private val oneSecondOrMinuteInterval = 360.0 / 60
    private val oneSecondOrMinuteRadian = Math.toRadians(oneSecondOrMinuteInterval).toFloat()
    private var stickColor = Color.Black
    var numberColor = Color.Black
    private var smallStroke = 2f
    private var bigStroke = 8f
    private var strokeCap = StrokeCap.Round
    private val numberPaint = android.graphics.Paint().apply {
        textSize = 60f
        color = Color.Red.toArgb()
    }
    private var currentRadian = Math.toRadians(90.0)
    private var textX = 0f
    private var textY = 0f
    private val rect = Rect()

    var relativeNumberBaseline = 0f
    fun draw(drawScope: DrawScope) {
        center = Offset(drawScope.size.width / 2f, drawScope.size.height / 2f)
        caseRadius = drawScope.size.minDimension / 2.2f
        outerEndStickCircleRadius = caseRadius - 10
        innerEndStickCircleRadius = caseRadius - 40
        numberCircleRadius = caseRadius - 50

        for (i in 0..29) { // draw sticks
            var strokeWidth = if (i.mod(5) == 0) bigStroke else smallStroke

            drawScope.drawSticks(
                Offset(
                    center.x - (cos(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius),
                    center.y + (sin(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius)
                ),
                Offset(
                    center.x - (cos(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius),
                    center.y + (sin(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius)
                ),
                strokeWidth,
            )

            drawScope.drawSticks(
                Offset(
                    center.x + (cos(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius),
                    center.y - (sin(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius)
                ),
                Offset(
                    center.x + (cos(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius),
                    center.y - (sin(oneSecondOrMinuteRadian * i) * outerEndStickCircleRadius)
                ),
                strokeWidth,
            )
        }

        for (i in fullClock downTo 7) { // draw numbers
            relativeNumberBaseline = (fullClock - i) / halfClock.toFloat()
            numberPaint.getTextBounds("$i", 0, "$i".length, rect)

            if (cos(currentRadian) in 0f..0.000001f) // since we are going counterclockwise from 12 to 7,
            // the only hour when cos equals 0 is 12, which is the first loop
            {
                textX =
                    center.x - rect.width() / 2 + rect.left
                textY =
                    center.y - sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * (1 - relativeNumberBaseline)
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "$i",
                        textX,
                        textY,
                        numberPaint
                    )
                }

                numberPaint.getTextBounds("${i - 6}", 0, "${i - 6}".length, rect)
                textY =
                    center.y + sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * relativeNumberBaseline
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText( // then we draw the opposite number
                        "${i - 6}",
                        textX,
                        textY,
                        numberPaint
                    )
                }
            } else {
                textX =
                    center.x + cos(currentRadian).toFloat() * innerEndStickCircleRadius
                textY =
                    center.y - sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * (1 - relativeNumberBaseline)
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "$i",
                        textX,
                        textY,
                        numberPaint
                    )
                }

                numberPaint.getTextBounds("${i - 6}", 0, "${i - 6}".length, rect)
                textX =
                    center.x - cos(currentRadian).toFloat() * innerEndStickCircleRadius - rect.width()
                textY =
                    center.y + sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * relativeNumberBaseline
                drawScope.drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "${i - 6}",
                        textX,
                        textY,
                        numberPaint
                    )
                }
            }

            currentRadian += oneSecondOrMinuteRadian * 5
        }
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
    Canvas(modifier = modifier, onDraw = { clockGraphics.draw(this) })
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
                center.x - (cos(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius),
                center.y + (sin(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius)
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
                center.x + (cos(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius),
                center.y - (sin(oneSecondOrMinuteRadian * i) * innerEndStickCircleRadius)
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
                center.y - sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * (1 - relativeNumberBaseline)
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
                center.y + sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * relativeNumberBaseline
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
                center.x + cos(currentRadian).toFloat() * innerEndStickCircleRadius
            textY =
                center.y - sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * (1 - relativeNumberBaseline)
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
                center.x - cos(currentRadian).toFloat() * innerEndStickCircleRadius - rect.width()
            textY =
                center.y + sin(currentRadian).toFloat() * innerEndStickCircleRadius - rect.top * relativeNumberBaseline
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
