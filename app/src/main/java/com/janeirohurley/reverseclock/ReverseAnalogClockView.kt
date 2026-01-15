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

    // Image redimensionnée (sera calculée dans onSizeChanged pour être responsive)
    private var scaledBitmap: Bitmap? = null

    // Dimensions responsives
    private var mRadius = 0f
    private var mCx = 0f
    private var mCy = 0f
    
    private var tickSizeHour = 0f
    private var tickSizeMinute = 0f
    private var handStrokeHour = 0f
    private var handStrokeSecond = 0f
    private var centerDotRadius = 0f
    
    // Offsets pour le positionnement
    private var tickRadiusOuter = 0f
    private var numberRadius = 0f
    private var textOffsetNtafatiro = 0f
    private var textOffsetDesigner = 0f
    private var numberAdjustment = 0f

    private val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        setShadowLayer(5f, 5f, 5f, Color.BLACK)
    }

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE  // 🔥 texte en gras
    }

    // 🎨 Variable pour le texte "Ntafatiro" en vert clair
    private val ntafatiroPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90EE90") // Light Green
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(5f, 5f, 5f, Color.BLACK)
    }

    // 🎨 Variable pour "designed by..." en petit
    private val designerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY 
        textAlign = Paint.Align.CENTER
        textSize = 2f
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            mCx = w / 2f
            mCy = h / 2f
            // Padding dynamique : ~8% du rayon max disponible
            val minDim = minOf(mCx, mCy)
            mRadius = minDim * 0.92f // Laisse un peu de marge
            
            // Mise à jour des tailles de texte et traits en fonction du rayon
            clockPaint.strokeWidth = mRadius * 0.015f
            
            numberPaint.textSize = mRadius * 0.16f
            ntafatiroPaint.textSize = mRadius * 0.10f
            designerPaint.textSize = mRadius * 0.06f
            
            // Tailles pour les ticks et aiguilles
            tickSizeHour = mRadius * 0.024f
            tickSizeMinute = mRadius * 0.01f
            
            handStrokeHour = mRadius * 0.04f
            handStrokeSecond = mRadius * 0.02f
            centerDotRadius = mRadius * 0.06f
            
            // Offsets
            tickRadiusOuter = mRadius * 0.95f
            numberRadius = mRadius * 0.74f // Ajusté pour correspondre à la position idéale
            
            textOffsetNtafatiro = mRadius * 0.08f // Marge par rapport à l'image
            textOffsetDesigner = mRadius * 0.10f
            numberAdjustment = mRadius * 0.07f

            
            // Calculer la taille de l'image de manière responsive
            // On vise une hauteur d'environ 90% du rayon de l'horloge
            val targetHeight = (mRadius * 0.9f).toInt()
            
            // Garder le ratio d'aspect de l'image originale
            val aspectRatio = centerBitmap.width.toFloat() / centerBitmap.height.toFloat()
            val targetWidth = (targetHeight * aspectRatio).toInt()
            
            if (targetWidth > 0 && targetHeight > 0) {
                scaledBitmap = Bitmap.createScaledBitmap(centerBitmap, targetWidth, targetHeight, true)
            }
        }
    }

    private fun drawTicks(canvas: Canvas) {
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6).toDouble())
            val isHourMark = i % 5 == 0
            val tickSize = if (isHourMark) tickSizeHour else tickSizeMinute
            
            // Distance responsive
            val distance = if (isHourMark) 0.98f else 0.98f 

            val x = (tickRadiusOuter * distance * sin(angle)).toFloat()
            val y = (-tickRadiusOuter * distance * cos(angle)).toFloat()

            canvas.drawCircle(x, y, tickSize, tickPaint)
        }
    }

    // Couleurs pour le thème clair (jour: 6h - 18h)
    private val lightBgColor = Color.parseColor("#F5F5F5")
    private val lightTextColor = Color.parseColor("#212121")
    private val lightTickColor = Color.parseColor("#212121")

    // Couleurs pour le thème sombre (nuit: 18h - 6h)
    private val darkBgColor = Color.parseColor("#121212")
    private val darkTextColor = Color.WHITE
    private val darkTickColor = Color.WHITE

    private fun isDayTime(): Boolean {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR) // Format 12h (0-11)
        val amPm = cal.get(Calendar.AM_PM) // 0 = AM, 1 = PM

        // Jour: 6h AM jusqu'à 6h PM (exclu)
        return if (amPm == Calendar.AM) {
            hour >= 6 || hour == 0 // 6-11 AM (hour 0 = 12 AM = minuit, donc nuit)
        } else {
            hour in 0..5 // 12 PM - 5 PM (0 = 12h, 1 = 13h, etc.)
        }
    }

    private fun applyTheme() {
        if (isDayTime()) {
            // Thème clair
            numberPaint.color = lightTextColor
            tickPaint.color = lightTickColor
            clockPaint.color = lightTextColor
            designerPaint.color = Color.DKGRAY
        } else {
            // Thème sombre
            numberPaint.color = darkTextColor
            tickPaint.color = darkTickColor
            clockPaint.color = darkTextColor
            designerPaint.color = Color.LTGRAY
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Appliquer le thème selon l'heure
        applyTheme()
        val bgColor = if (isDayTime()) lightBgColor else darkBgColor
        canvas.drawColor(bgColor)

        // Utilisation de l'image redimensionnée responsive
        val currentBitmap = scaledBitmap
        if (currentBitmap != null) {
            // 🔹 dessiner l'image centrée et rotative
            canvas.save()                          // sauvegarde l'état
            canvas.translate(mCx, mCy)             // déplacer le centre
            val rotationAngle = -11f               // angle en degrés
            canvas.rotate(rotationAngle)           // rotation autour du centre
            canvas.drawBitmap(
                currentBitmap,
                -currentBitmap.width / 2f,
                -currentBitmap.height / 2f,
                null
            )
            canvas.restore()                        // revient à l'état initial

            // 🔹 dessiner les textes par rapport à l'image
            canvas.translate(mCx, mCy)

            // ✍️ AJOUT: "Ntafatiro" en haut de la carte
            canvas.drawText("Ntafatiro", 0f, -currentBitmap.height / 2f - textOffsetNtafatiro, ntafatiroPaint)

            // ✍️ AJOUT: "designed by..." en bas de la carte
            canvas.drawText("Designed  and developed by ", 0f, currentBitmap.height / 2f + textOffsetDesigner, designerPaint)
            canvas.drawText("Gasape Group Innovation LTD", 0f, currentBitmap.height / 2f + textOffsetDesigner + 30f, designerPaint)
        } else {
            // Fallback
            canvas.translate(mCx, mCy)
        }

        drawNumbers(canvas)
        drawHands(canvas)
        drawTicks(canvas)

        drawCircle(canvas, mRadius, Color.WHITE)
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
    private fun drawNumbers(canvas: Canvas) {
        for (i in 0..11) {
            val angle = Math.toRadians((i * 30).toDouble())
            val displayNumber = if (i == 0) 12 else 12 - i

            // Ajustement dynamique du rayon pour les petits nombres
            val finalNumberRadius = if (displayNumber < 10) numberRadius + numberAdjustment else numberRadius

            val x = (finalNumberRadius * sin(angle)).toFloat()
            val y = (-finalNumberRadius * cos(angle)).toFloat()

            canvas.save()
            canvas.translate(x, y)
            canvas.rotate((i).toFloat()) // pour que le texte reste lisible
            // offset vertical pour centrer le texte responsive
            val textVOffset = numberPaint.textSize * 0.3f
            canvas.drawText(displayNumber.toString(), 0f, textVOffset, numberPaint)
            canvas.restore()
        }
    }

    /**
     * 🕰️ Aiguilles inversées + fluide
     */
    private fun drawHands(canvas: Canvas) {

        val cal = Calendar.getInstance()

        val seconds = cal.get(Calendar.SECOND)
        val minutes = cal.get(Calendar.MINUTE) + seconds / 60f
        var hours = cal.get(Calendar.HOUR) + minutes / 60f
        val amPm = cal.get(Calendar.AM_PM)

        // Si c'est PM, décaler de 6 heures (180°)
        // 12h PM → 6, 1h PM → 7, ... 6h PM → 12
        if (amPm == Calendar.PM) {
            hours = (hours + 6) % 12
        }

        val secondAngle = Math.toRadians((-seconds * 6).toDouble())
        val minuteAngle = Math.toRadians((-minutes * 6).toDouble())
        val hourAngle = Math.toRadians((-hours * 30).toDouble())

        val handColor = if (isDayTime()) lightTextColor else darkTextColor
        drawHand(canvas, hourAngle, mRadius * 0.6f, handColor, handStrokeHour, mRadius*0.25f)
        drawHand(canvas, minuteAngle, mRadius * 0.85f, handColor, handStrokeHour, mRadius*0.25f)
        drawCircle(canvas, centerDotRadius, handColor, true)
        drawHand(canvas, secondAngle, mRadius * 0.9f, Color.RED, handStrokeSecond, mRadius*0.25f)
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
