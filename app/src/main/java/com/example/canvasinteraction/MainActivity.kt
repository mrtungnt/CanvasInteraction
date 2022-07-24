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
    val circle = Circle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanvasInteractionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
//                    Canvas(modifier = Modifier.pointerInput(key1 = Unit) {
//                        detectTapGestures {
//                            if (offsetWithinCircle(it, circle.center, circle.radius))
//                                println("circle was clicked: $it")
//                        }
//                    }, onDraw = onDraw)

                    Canvas(modifier = Modifier) {
                        drawIntoCanvas {
                            val text = "189 Abc xyz"
                            val paint = android.graphics.Paint().apply {
                                textSize = 50f
                                color = Color.Red.toArgb()
                            }
                            val rect = Rect()
                            paint.getTextBounds(text, 0, text.length, rect)
                            val baseline = -rect.top.toFloat() + 10
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
                        }
                    }
                }
            }
        }
    }

    private val onDraw: DrawScope.() -> Unit = {
        circle.center = Offset(
            size.width / 2f, size.height / 2f
        )
        circle.radius = size.minDimension / 3f
        /*drawCircle(
            color = Color.Green, radius = circle.radius, center = circle.center
        )*/
        val onePortionDeg = 360.0 / 60
        val radOf1PortionDeg = Math.toRadians(onePortionDeg).toFloat()
        val radOfInnerEndStickCircle = circle.radius + 60
        drawCircle(
            color = Color.LightGray,
            radius = radOfInnerEndStickCircle,
            center = circle.center
        )
        val radOfOuterEndStickCircle = circle.radius + 90
        val numberCircleRad = circle.radius + 30
        for (i in 0..29) { // draw sticks
            /*drawCircle(
                color = Color.Magenta,
                center = Offset(
                    circle.center.x + (cos(radOf1PortionDeg * i) * radOfInnerEndStickCircle),
                    circle.center.y - (sin(radOf1PortionDeg * i) * radOfInnerEndStickCircle)
                ),
                radius = 8f
            )
            drawCircle(
                color = Color.Magenta,
                center = Offset(
                    circle.center.x - (cos(radOf1PortionDeg * i) * radOfInnerEndStickCircle),
                    circle.center.y + (sin(radOf1PortionDeg * i) * radOfInnerEndStickCircle)
                ),
                radius = 8f
            )*/
            drawLine(
                color = Color.Magenta,
                start = Offset(
                    circle.center.x - (cos(radOf1PortionDeg * i) * radOfInnerEndStickCircle),
                    circle.center.y + (sin(radOf1PortionDeg * i) * radOfInnerEndStickCircle)
                ),
                end = Offset(
                    circle.center.x - (cos(radOf1PortionDeg * i) * radOfOuterEndStickCircle),
                    circle.center.y + (sin(radOf1PortionDeg * i) * radOfOuterEndStickCircle)
                ),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color.Magenta,
                start = Offset(
                    circle.center.x + (cos(radOf1PortionDeg * i) * radOfInnerEndStickCircle),
                    circle.center.y - (sin(radOf1PortionDeg * i) * radOfInnerEndStickCircle)
                ),
                end = Offset(
                    circle.center.x + (cos(radOf1PortionDeg * i) * radOfOuterEndStickCircle),
                    circle.center.y - (sin(radOf1PortionDeg * i) * radOfOuterEndStickCircle)
                ),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
        }

        val paint = android.graphics.Paint().apply {
            textSize = 12.dp.toPx()
            color = Color.Red.toArgb()
        }

        var radian = Math.toRadians(90.0)
        var textX = 0f
        var textY = 0f
        val rect = Rect(0, 0, 10, 10)
        for (i in 12 downTo 7) { // draw numbers
            paint.getTextBounds("$i", 0, "$i".length, rect)
            textX =
                circle.center.x + cos(radian).toFloat() * radOfInnerEndStickCircle
            textY =
                circle.center.y + sin(radian).toFloat() * radOfInnerEndStickCircle - rect.height() / 2
            if (cos(radian) in 0f..0.0001f) // since we are going counterclockwise from 12 to 7,
            // the only one time when cos equals 0 is 12, which is the first loop
            {
                textX =
                    circle.center.x + cos(radian).toFloat() * radOfInnerEndStickCircle - rect.width() / 2
                textY =
                    circle.center.y - sin(radian).toFloat() * radOfInnerEndStickCircle + rect.height()
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
                    circle.center.x - cos(radian).toFloat() * radOfInnerEndStickCircle - rect.width() / 2
                textY =
                    circle.center.y + sin(radian).toFloat() * radOfInnerEndStickCircle
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "${i - 6}",
                        textX,
                        textY,
                        paint
                    )
                } // then we draw the opposite number
            } else {
                textX =
                    circle.center.x + cos(radian).toFloat() * radOfInnerEndStickCircle
                textY =
                    circle.center.y - sin(radian).toFloat() * radOfInnerEndStickCircle + rect.height() / 2
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "$i",
                        textX,
                        textY,
                        paint
                    )
                }
//                                drawRect(
//                                    color = Color.Red,
//                                    Offset(
//                                        textX,
//                                        circle.center.y - sin(radian).toFloat() * radOfInnerEndStickCircle + rect.top.toFloat()
//                                    ),
//                                    size = Size(rect.width().toFloat(), rect.height().toFloat()),
//                                    alpha = 1f,
//                                    style = Fill,
//                                )
                val color = Color.Green
                drawLine(
                    color,
                    Offset(
                        textX,
                        textY + rect.top
                    ),
                    Offset(
                        textX + rect.width(),
                        textY + rect.top
                    ),
                )

                drawLine(
                    color,
                    Offset(
                        textX + rect.width(),
                        textY + rect.top
                    ),
                    Offset(
                        textX + rect.width(),
                        textY - rect.bottom
                    ),
                )

                drawLine(
                    color,
                    Offset(
                        textX + rect.width(),
                        textY - rect.bottom
                    ),
                    Offset(
                        textX,
                        textY - rect.bottom
                    ),
                )

                drawLine(
                    color,
                    Offset(
                        textX,
                        textY - rect.bottom
                    ),
                    Offset(
                        textX,
                        textY + rect.top
                    ),
                )



                paint.getTextBounds("${i - 6}", 0, "${i - 6}".length, rect)
                textX =
                    circle.center.x - cos(radian).toFloat() * radOfInnerEndStickCircle - rect.width()
                textY =
                    circle.center.y + sin(radian).toFloat() * radOfInnerEndStickCircle + rect.height() / 2
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        "${i - 6}",
                        textX,
                        textY,
                        paint
                    )
                }
            }

            radian += radOf1PortionDeg * 5
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

data class Circle(var center: Offset = Offset(0f, 0f), var radius: Float = 0f)