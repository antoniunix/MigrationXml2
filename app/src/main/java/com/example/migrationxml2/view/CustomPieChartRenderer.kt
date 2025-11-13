package com.example.migrationxml2.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.example.migrationxml2.R
import com.example.migrationxml2.model.ItemCompositionGraphModel
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CustomPieChartRenderer (
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    iconSize: Int,
) : PieChartRenderer(chart, animator, viewPortHandler) {

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(chart.context, R.color.inverse_01)
    }

    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.DKGRAY
        textSize = 36f
    }

    private val customPath = Path()
    private val iconSizeGlobal = iconSize

    var isGrayOutActive: Boolean = true
    var unselectedAlpha = DEFAULT_UNSELECTED_ALPHA

    var centerTitle: String = EMPTY
    var centerTitleSize: Float = 36f
    private val minTextSize = 18f

    override fun drawDataSet(c: Canvas, dataSet: IPieDataSet) {
        val chart = mChart
        val rotationAngle = chart.rotationAngle
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val center = chart.centerCircleBox
        val drawAngles = chart.drawAngles
        val circleBox = chart.circleBox

        val sliceSpace = dataSet.sliceSpace
        val radius = chart.radius
        val hasOnlyOneSlice = dataSet.entryCount == 1
        val highlightedIndex = chart.highlighted?.firstOrNull()?.x?.toInt() ?: -1
        var angle = rotationAngle

        for (i in 0 until dataSet.entryCount) {
            val sliceAngle = drawAngles[i]
            val sweepAngle = sliceAngle * phaseY
            val entry = dataSet.getEntryForIndex(i)
            val isHighlighted = i == highlightedIndex
            val originalColor = dataSet.getColor(i)

            mRenderPaint.color = ColorUtils.setAlphaComponent(originalColor, unselectedAlpha).takeIf {
                shouldBeAppliedGrayOut(highlightedIndex, isHighlighted)
            } ?: originalColor

            val spaceAngle: Float = if (sliceSpace > 0f) {
                (sliceSpace / (PI * radius).toFloat()) * RADIANS_TO_DEGREES
            } else 0f
            val startAngle = angle + spaceAngle / 2f
            val sweep = sweepAngle - spaceAngle
            customPath.reset()

            if (hasOnlyOneSlice) {
                customPath.moveTo(center.x, center.y)
                customPath.arcTo(circleBox, rotationAngle, ALMOST_FULL_CIRCLE_ANGLE)
                customPath.lineTo(center.x, center.y)
                customPath.close()
            } else if (sweep > 0f) {
                customPath.arcTo(circleBox, startAngle, sweep)
                customPath.lineTo(center.x, center.y)
                customPath.close()
            }

            c.drawPath(customPath, mRenderPaint)
            angle += sliceAngle * phaseX
        }
    }

    override fun drawValues(c: Canvas) {
        val chart = mChart
        val center = chart.centerCircleBox
        val radius = chart.radius
        val rotationAngle = chart.rotationAngle
        val drawAngles = chart.drawAngles
        val absoluteAngles = chart.absoluteAngles

        val data = chart.data ?: return
        val dataSet = data.dataSet
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        var angle = rotationAngle
        val rectF = RectF()
        val highlightedIndex = chart.highlighted?.firstOrNull()?.x?.toInt() ?: -1

        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i) as? PieEntry ?: continue
            val sliceAngle = drawAngles[i]
            val transformedAngle = angle + sliceAngle / 2f

            val icon = entry.icon ?: continue
            val sliceSpace = dataSet.sliceSpace

            val outerRadius = chart.radius
            val holeRadius = chart.holeRadius / 100f * outerRadius
            var contentPositionFactor = 0.5f // Middle of slice
            val contentRadius = holeRadius + (outerRadius - holeRadius) * contentPositionFactor

            val isHighlighted = i == highlightedIndex

            val x = center.x + contentRadius * cos(Math.toRadians(transformedAngle.toDouble())).toFloat()
            val y = center.y + contentRadius * sin(Math.toRadians(transformedAngle.toDouble())).toFloat()

            val compositionModel = entry.data as? ItemCompositionGraphModel

            // Draw icon
            val iconSize = iconSizeGlobal
            if (entry.value >= MIN_VALUE_DRAW_ICON) {
                icon.setBounds(
                    (x - iconSize / 2).toInt(),
                    (y - iconSize / 2 - 10).toInt(),
                    (x + iconSize / 2).toInt(),
                    (y + iconSize / 2 - 10).toInt()
                )
                val originalIconColor = ContextCompat.getColor(
                    chart.context,
                    R.color.inverse_01
                )
                val iconColor = ColorUtils.setAlphaComponent(originalIconColor, unselectedAlpha).takeIf {
                    shouldBeAppliedGrayOut(highlightedIndex, isHighlighted)
                } ?: originalIconColor

                icon.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
                icon.draw(c)
            }

            // Draw value
            val displayValue = entry.label
            val originalLabelColor = chart.resources.getColor(
                R.color.inverse_01
            )
            iconPaint.color = ColorUtils.setAlphaComponent(originalLabelColor, unselectedAlpha).takeIf {
                shouldBeAppliedGrayOut(highlightedIndex, isHighlighted)
            } ?: originalLabelColor

            if (entry.value >= MIN_VALUE_DRAW_LABEL) {
                c.drawText(displayValue, x, y + TEXT_BELOW_ICON_OFFSET_Y, iconPaint)
            }

            angle += sliceAngle * phaseX
        }

        // Draw the title centered with dynamic adjustment
        if (centerTitle.isNotEmpty()) {
            val holeRadiusPx = (mChart.holeRadius / 100f) * mChart.radius
            var textSize = centerTitleSize
            centerTextPaint.textSize = textSize

            val maxTextWidth = holeRadiusPx * 1.6f
            val availableHeight = holeRadiusPx * 1.6f

            val words = centerTitle.split(" ")
            var lines: List<String>

            // Dynamic text size adjustment
            while (true) {
                centerTextPaint.textSize = textSize
                lines = wrapText(words, centerTextPaint, maxTextWidth)
                val totalHeight = lines.size * centerTextPaint.textSize * 1.2f

                if (totalHeight <= availableHeight || textSize <= minTextSize) break
                textSize -= 2f
            }

            // Draw the lines centered vertically
            val lineHeight = centerTextPaint.textSize * 1.2f
            val totalHeight = lines.size * lineHeight
            var y = center.y - totalHeight / 2 + lineHeight / 2

            for (line in lines) {
                c.drawText(line, center.x, y, centerTextPaint)
                y += lineHeight
            }
        }
    }

    private fun wrapText(words: List<String>, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = EMPTY

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        return lines
    }

    private fun shouldBeAppliedGrayOut(
        highlightedIndex: Int,
        isHighlighted: Boolean,
    ) = isGrayOutActive && highlightedIndex != -1 && !isHighlighted

    private fun getFormattedValue(value: Float): String {
        val strValue = when {
            value % 1 == 0f -> {
                String.format("%.0f%%", value)
            }
            value * 10 % 10 == 0f -> {
                String.format("%.1f%%", value)
            }
            else -> {
                String.format("%.2f%%", value)
            }
        }
        return when {
            value <= MIN_VALUE_DRAW_LABEL -> EMPTY
            else -> strValue
        }
    }

    override fun drawHighlighted(c: Canvas, indices: Array<out Highlight>?) {
        super.drawHighlighted(c, indices)
    }

    fun applyTextAppearance(@StyleRes styleResId: Int) {
        val typedArray = mChart.context.obtainStyledAttributes(styleResId, androidx.appcompat.R.styleable.TextAppearance)

        try {
            val textSize = typedArray.getDimensionPixelSize(
                androidx.appcompat.R.styleable.TextAppearance_android_textSize, 32
            )
            val textColor = typedArray.getColor(
                androidx.appcompat.R.styleable.TextAppearance_android_textColor, Color.WHITE
            )

            val fontId = typedArray.getResourceId(androidx.appcompat.R.styleable.TextAppearance_android_fontFamily, 0)
            val typeface = if (fontId != 0) {
                mChart.context.resources.getFont(fontId)
            } else {
                Typeface.DEFAULT
            }

            val textStyle = typedArray.getInt(androidx.appcompat.R.styleable.TextAppearance_android_textStyle, Typeface.NORMAL)

            iconPaint.apply {
                this.textSize = textSize.toFloat()
                this.color = textColor
                this.typeface = Typeface.create(typeface, textStyle)
            }

        } finally {
            typedArray.recycle()
        }
    }

    companion object {
        private const val RADIANS_TO_DEGREES = 180f
        // Canvas may not render a full 360° arc correctly, so we use a slightly smaller value
        private const val ALMOST_FULL_CIRCLE_ANGLE = 359.9999f
        private const val TEXT_BELOW_ICON_OFFSET_Y = 60f
        private const val DEFAULT_UNSELECTED_ALPHA = 70
        private const val MIN_VALUE_DRAW_ICON = 2.51
        private const val MIN_VALUE_DRAW_LABEL = 7.00
        private const val EMPTY = ""
    }
}

