package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacedStickerDao {
    @Query("SELECT * FROM placed_stickers")
    fun getAllPlacedStickers(): Flow<List<PlacedSticker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedSticker(sticker: PlacedSticker): Long

    @Update
    suspend fun updatePlacedSticker(sticker: PlacedSticker)

    @Query("DELETE FROM placed_stickers WHERE id = :id")
    suspend fun deletePlacedSticker(id: Int)

    @Query("DELETE FROM placed_stickers")
    suspend fun clearAllPlacedStickers()
}
