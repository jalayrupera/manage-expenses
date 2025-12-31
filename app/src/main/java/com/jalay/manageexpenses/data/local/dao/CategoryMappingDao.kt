package com.jalay.manageexpenses.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jalay.manageexpenses.data.local.entity.CategoryMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: CategoryMappingEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mappings: List<CategoryMappingEntity>)

    @Delete
    suspend fun delete(mapping: CategoryMappingEntity)

    @Query("SELECT * FROM category_mappings")
    fun getAllMappings(): Flow<List<CategoryMappingEntity>>

    @Query("SELECT * FROM category_mappings ORDER BY category, keyword")
    suspend fun getAllMappingsSync(): List<CategoryMappingEntity>

    @Query("SELECT * FROM category_mappings WHERE keyword = :keyword LIMIT 1")
    suspend fun getMappingByKeyword(keyword: String): CategoryMappingEntity?

    @Query("SELECT * FROM category_mappings WHERE category = :category")
    fun getMappingsByCategory(category: String): Flow<List<CategoryMappingEntity>>

    @Query("DELETE FROM category_mappings WHERE isCustom = 1")
    suspend fun deleteCustomMappings()

    @Query("SELECT DISTINCT category FROM category_mappings ORDER BY category")
    suspend fun getAllCategories(): List<String>
}