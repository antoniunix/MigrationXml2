// kotlin
package com.example.migrationxml2

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.migrationxml2.databinding.MainActivityBinding
import com.example.migrationxml2.model.CompositionGraphModel
import com.example.migrationxml2.model.FakeMockCustomPieChartProvider

class MainActivity : ComponentActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpPieChart(FakeMockCustomPieChartProvider.compositionGraphModel())
    }

    private fun setUpPieChart(model: CompositionGraphModel) {
        binding.examplePieChartView.setUpPieChart(model)
    }
}
