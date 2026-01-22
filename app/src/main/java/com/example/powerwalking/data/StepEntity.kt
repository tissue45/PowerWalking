// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD 형식
    val steps: Int,
    val timestamp: Long = Date().time
)
