package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "adventure_saves")
data class AdventureSaveState(
    @PrimaryKey val slotId: String, // "slot_1", "slot_2", "slot_3", "autosave"
    val saveName: String, // Custom descriptor e.g. "Nova - Space Quest"
    val characterName: String,
    val hairColor: String,
    val clothingStyle: String,
    val backstory: String,
    val currentPrompt: String,
    val choicesJson: String, // e.g. serialized JSON or a list
    val historyJson: String, // chronological list of dialog/transcript
    val sceneImageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AdventureSaveStateDao {
    @Query("SELECT * FROM adventure_saves ORDER BY timestamp DESC")
    fun getAllSaves(): Flow<List<AdventureSaveState>>

    @Query("SELECT * FROM adventure_saves WHERE slotId = :slotId LIMIT 1")
    suspend fun getSaveBySlot(slotId: String): AdventureSaveState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSave(save: AdventureSaveState)

    @Query("DELETE FROM adventure_saves WHERE slotId = :slotId")
    suspend fun deleteSaveBySlot(slotId: String)
}
