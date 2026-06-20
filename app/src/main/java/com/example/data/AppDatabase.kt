package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserProfile::class, GeneratedStoryContent::class, AdventureSaveState::class, PlacedSticker::class, CustomHero::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun generatedStoryDao(): GeneratedStoryDao
    abstract fun adventureSaveStateDao(): AdventureSaveStateDao
    abstract fun placedStickerDao(): PlacedStickerDao
    abstract fun customHeroDao(): CustomHeroDao
}

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            preloadedStoryContents.forEach { story ->
                                db.execSQL(
                                    "INSERT OR REPLACE INTO generated_stories (id, title, content, coverImageUrl, category, createdAt) VALUES (?, ?, ?, ?, ?, ?)",
                                    arrayOf<Any>(story.id, story.title, story.content, story.coverImageUrl, story.category, story.createdAt)
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
            INSTANCE = instance
            instance
        }
    }
}
