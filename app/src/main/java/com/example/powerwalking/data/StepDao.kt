// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE date = :date")
    fun getStepsByDateSync(date: String): StepEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: StepEntity)
}
