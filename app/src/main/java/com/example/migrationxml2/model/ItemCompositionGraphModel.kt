package com.example.migrationxml2.model

data class ItemCompositionGraphModel (
    val id: Int,
    val value: Double,
    val labelValue: String? = null,
    var labelColor: Int? = null,
    val icon: Int? = null,
    val iconColor: Int? = null,
    val colorBackground: Int? = null,
    val onItemCellListener: (item: ItemCompositionGraphModel) -> Unit = {}
)