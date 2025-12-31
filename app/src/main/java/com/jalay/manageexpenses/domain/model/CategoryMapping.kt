package com.jalay.manageexpenses.domain.model

data class CategoryMapping(
    val id: Long? = null,
    val keyword: String,
    val category: String,
    val icon: String,
    val isCustom: Boolean = false
)