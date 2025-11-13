package com.example.migrationxml2.model

data class CompositionGraphModel(
    val items: List<ItemCompositionGraphModel>,
    val label: String? = null,
    val unselectedAlpha: Int? = null,
    val holeRadius: Float? = null,
    val iconSize: Int = 70,
)