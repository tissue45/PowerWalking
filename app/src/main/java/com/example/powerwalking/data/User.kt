// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val score: Int,
    val attack: Int = 0,
    val defense: Int = 0,
    val health: Int = 0
)

