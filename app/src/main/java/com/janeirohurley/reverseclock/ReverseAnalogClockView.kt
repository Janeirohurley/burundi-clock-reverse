package com.janeirohurley.reverseclock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class ReverseAnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val centerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.burundi)

    val newWidth = 600  // largeur désirée en pixels
    val newHeight = 690 // hauteur désirée en pixels

    val scaledBitmap = Bitmap.createScaledBitmap(centerBitmap, newWidth, newHeight, true)
    private val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
        setShadowLayer(5f, 5f, 5f, Color.BLACK)
    }

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 80f   // taille
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE  // 🔥 texte en gras
    }


    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL

    }

    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        setShadowLayer(5f, 5f, 5f, Color.BLACK)
    }

    private fun drawTicks(canvas: Canvas, tickRadiusOuter: Float) {

        for (i in 0 until 60) {

            val angle = Math.toRadians((i * 6).toDouble())

            val isHourMark = i % 5 == 0
            val tickSize = if (isHourMark) 12f else 5f
            val distance = if (isHourMark) 0.98f else 0.98f // proportion du rayon

            val x = (tickRadiusOuter * distance * sin(angle)).toFloat()
            val y = (-tickRadiusOuter * distance * cos(angle)).toFloat()

            canvas.drawCircle(x, y, tickSize, tickPaint)
        }
    }

    private val bgColor = Color.parseColor("#121212")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(bgColor)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) - 40      // rayon du cercle

        val tickRadiusOuter = radius - 25     // rayon utilisé pour dessiner les ticks
        val numberRadius = radius - 130       // rayon pour dessiner les nombres

        // 🔹 dessiner l'image centrée et rotative
        canvas.save()                          // sauvegarde l'état
        canvas.translate(cx, cy)                // déplacer le centre
        val rotationAngle = -11f                 // angle en degrés
        canvas.rotate(rotationAngle)            // rotation autour du centre
        canvas.drawBitmap(
            scaledBitmap,
            -scaledBitmap.width / 2f,
            -scaledBitmap.height / 2f,
            null
        )
        canvas.restore()                        // revient à l'état initial

        // 🔹 dessiner le reste de l'horloge
        canvas.translate(cx, cy)


        drawNumbers(canvas, numberRadius)
        drawHands(canvas, radius)
        drawTicks(canvas, tickRadiusOuter)


        drawCircle(canvas, radius, Color.WHITE,)
        postInvalidateDelayed(1000)  // tick des secondes
    }

    private fun drawCircle(canvas: Canvas, radius: Float, color: Int, fill: Boolean = false) {
        clockPaint.color = color
        clockPaint.style = if (fill) Paint.Style.FILL else Paint.Style.STROKE
        canvas.drawCircle(0f, 0f, radius, clockPaint)
    }

    /**
     * 🔢 Chiffres inversés MAIS lisibles
     */
    private fun drawNumbers(canvas: Canvas, baseRadius: Float) {
        for (i in 0..11) {
            val angle = Math.toRadians((i * 30).toDouble())
            val displayNumber = if (i == 0) 12 else 12 - i

            // Ajustement dynamique du rayon
            val numberRadius = if (displayNumber < 10) baseRadius + 35f else baseRadius

            val x = (numberRadius * sin(angle)).toFloat()
            val y = (-numberRadius * cos(angle)).toFloat()

            canvas.save()
            canvas.translate(x, y)
            canvas.rotate((i).toFloat()) // pour que le texte reste lisible
            canvas.drawText(displayNumber.toString(), 0f, 15f, numberPaint)
            canvas.restore()
        }
    }




    /**
     * 🕰️ Aiguilles inversées + fluide
     */
    private fun drawHands(canvas: Canvas, radius: Float) {

        val cal = Calendar.getInstance()
        val radius2 = minOf(30f,30f)

        val seconds = cal.get(Calendar.SECOND)
        val minutes = cal.get(Calendar.MINUTE) + seconds / 60f
        val hours = cal.get(Calendar.HOUR) + minutes / 60f

        val secondAngle = Math.toRadians((-seconds * 6).toDouble())
        val minuteAngle = Math.toRadians((-minutes * 6).toDouble())
        val hourAngle = Math.toRadians((-hours * 30).toDouble())

        drawHand(canvas, hourAngle, radius * 0.6f, Color.WHITE, 20f,radius*0.25f)
        drawHand(canvas, minuteAngle, radius * 0.85f, Color.WHITE, 20f,radius*0.25f)
        drawCircle(canvas, radius2,Color.WHITE, true)
        drawHand(canvas, secondAngle, radius * 0.9f, Color.RED, 10f,radius*0.25f)
    }
    private fun drawHand(
        canvas: Canvas,
        angle: Double,
        length: Float,
        color: Int,
        stroke: Float,
        backOffset: Float = 0f // distance pour dépasser le centre
    ) {
        handPaint.color = color
        handPaint.strokeWidth = stroke
        handPaint.strokeCap = Paint.Cap.ROUND

        val xEnd = (length * sin(angle)).toFloat()
        val yEnd = (-length * cos(angle)).toFloat()

        // Point derrière le centre
        val xStart = (-backOffset * sin(angle)).toFloat()
        val yStart = (backOffset * cos(angle)).toFloat()

        canvas.drawLine(xStart, yStart, xEnd, yEnd, handPaint)
    }

}
