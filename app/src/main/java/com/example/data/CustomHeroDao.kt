package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomHeroDao {
    @Query("SELECT * FROM custom_heroes ORDER BY createdAt DESC")
    fun getAllCustomHeroes(): Flow<List<CustomHero>>

    @Query("SELECT * FROM custom_heroes WHERE id = :id LIMIT 1")
    suspend fun getHeroById(id: Int): CustomHero?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHero(hero: CustomHero)

    @Query("DELETE FROM custom_heroes WHERE id = :id")
    suspend fun deleteHeroById(id: Int)
}
