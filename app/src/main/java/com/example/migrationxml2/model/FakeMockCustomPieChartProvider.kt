package com.example.migrationxml2.model

import com.example.migrationxml2.R

object FakeMockCustomPieChartProvider {
    fun compositionGraphModel() = CompositionGraphModel(
        items = listOf(
            ItemCompositionGraphModel(
                id = 1,
                value = 27.0,
                labelValue = "27%",
                labelColor = R.color.purple_200,
                icon = R.drawable.ic_launcher_foreground,
                iconColor = R.color.teal_200,
                colorBackground = R.color.teal_200,
            ),
            ItemCompositionGraphModel(
                id = 2,
                value = 25.0,
                labelValue = "25%",
                labelColor = R.color.purple_700,
                icon = R.drawable.ic_launcher_foreground,
                iconColor = R.color.white,
                colorBackground = R.color.purple_500,
            ),
            ItemCompositionGraphModel(
                id = 3,
                value = 15.0,
                labelValue = "15%",
                labelColor = R.color.purple_700,
                icon = R.drawable.ic_launcher_foreground,
                iconColor = R.color.teal_200,
                colorBackground = R.color.purple_700,
            ),
            ItemCompositionGraphModel(
                id = 4,
                value = 11.0,
                labelValue = "11%",
                labelColor = R.color.purple_700,
                icon = R.drawable.ic_launcher_foreground,
                iconColor = R.color.black,
                colorBackground = R.color.purple_500,
            ),
        ),
        label = "Composición de portafolio",
        unselectedAlpha = 70,
        holeRadius = 40f,
    )
}