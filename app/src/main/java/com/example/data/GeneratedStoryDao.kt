package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedStoryDao {
    @Query("SELECT * FROM generated_stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<GeneratedStoryContent>>

    @Query("SELECT * FROM generated_stories WHERE id = :id LIMIT 1")
    fun getStoryById(id: String): Flow<GeneratedStoryContent?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: GeneratedStoryContent)
}
