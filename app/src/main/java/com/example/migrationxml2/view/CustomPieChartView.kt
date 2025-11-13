package com.example.migrationxml2.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.migrationxml2.R
import com.example.migrationxml2.databinding.CustomPieChartBinding
import com.example.migrationxml2.model.CompositionGraphModel
import com.example.migrationxml2.model.ItemCompositionGraphModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class CustomPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    private var binding = CustomPieChartBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var customRenderer: CustomPieChartRenderer
    private var entries: List<PieEntry> = emptyList()
    private var pieChart: PieChart? = null
    var onSliceHighlight: (id: Int) -> Unit = {}

    fun setUpPieChart(compositionGraphModel: CompositionGraphModel) {
        pieChart = binding.pieChart

        pieChart?.let { pieChart ->
            customRenderer =
                CustomPieChartRenderer(
                    pieChart,
                    pieChart.animator,
                    pieChart.viewPortHandler,
                    compositionGraphModel.iconSize,
                ).apply {
                    centerTitle = compositionGraphModel.label.toString()
                    centerTitleSize = CENTER_TITLE_SIZE
                    compositionGraphModel.unselectedAlpha?.let { alpha ->
                        unselectedAlpha = alpha
                    }
                    //applyTextAppearance(R.style.bluebox_caption02_inverse)
                }
            pieChart.renderer = customRenderer

            entries = compositionGraphModel.items.map { item ->
                val icon = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_launcher_background
                )
                PieEntry(
                    item.value.toFloat(),
                    item.labelValue,
                    icon,
                    item
                )
            }

            val dataSet = PieDataSet(entries, compositionGraphModel.label).apply {
                setDrawIcons(true)
                selectionShift = 0f
                sliceSpace = 2f
                colors = compositionGraphModel.items.map { item ->
                    resources.getColor(R.color.black)
                }
                valueTextColor = Color.WHITE
                valueTextSize = 12f
                setDrawValues(true)
            }

            val pieData = PieData(dataSet)

            pieChart.apply {
                data = pieData
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = compositionGraphModel.holeRadius ?: HOLE_RADIUS_DEFAULT
                transparentCircleRadius = holeRadius
                setTransparentCircleAlpha(0)
                setUsePercentValues(false)
                setEntryLabelColor(Color.WHITE)
                setEntryLabelTextSize(12f)
                legend.isEnabled = false
                highlightValues(null)
                invalidate()

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        if (e is PieEntry) {
                            val data = e.data as? ItemCompositionGraphModel
                            onSliceHighlight(data?.id?:0)
                            data?.onItemCellListener?.let { it(data) }
                            showCellGrayout(pieChart)
                        }
                    }

                    override fun onNothingSelected() {
                        Log.d("PieChart", "No selection")
                    }
                })
            }
        }
    }

    fun onItemPieChartSelected(position: Int) {
        val limit = entries.size - 1
        pieChart?.highlightValue(position.toFloat(), 0).takeIf { position in 0..limit }
    }

    private fun showCellGrayout(pieChart: PieChart) {
        pieChart.apply {
            customRenderer.isGrayOutActive = true
            invalidate()

            pieChart.postDelayed({
                customRenderer.isGrayOutActive = false
                highlightValues(null)
                invalidate()
            }, GRAY_OUT_DURATION_MS)
        }
    }

    companion object {
        private const val GRAY_OUT_DURATION_MS = 1000L
        private const val CENTER_TITLE_SIZE = 34f
        private const val HOLE_RADIUS_DEFAULT = 40f
    }
}