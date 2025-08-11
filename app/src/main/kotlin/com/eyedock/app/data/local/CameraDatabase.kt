package com.eyedock.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.eyedock.app.data.local.dao.CameraDao
import com.eyedock.app.data.local.entity.CameraEntity
import com.eyedock.app.data.local.converter.DateConverter
import com.eyedock.app.utils.Constants

@Database(
    entities = [CameraEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class CameraDatabase : RoomDatabase() {
    
    abstract fun cameraDao(): CameraDao
    
    companion object {
        @Volatile
        private var INSTANCE: CameraDatabase? = null
        
        fun getDatabase(context: Context): CameraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CameraDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
