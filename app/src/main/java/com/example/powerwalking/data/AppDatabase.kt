// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, StepEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun stepDao(): StepDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "powerwalking_database"
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries() // 디버깅용: 메인 스레드 접근 허용
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
