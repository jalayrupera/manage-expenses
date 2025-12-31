package com.jalay.manageexpenses.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_mappings",
    indices = [
        Index(value = ["keyword"], unique = true)
    ]
)
data class CategoryMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val keyword: String,
    val category: String,
    val icon: String,
    val isCustom: Boolean
)