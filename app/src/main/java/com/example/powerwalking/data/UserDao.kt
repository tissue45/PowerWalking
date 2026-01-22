// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY score DESC")
    fun getAllUsersOrderedByScore(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET score = :score WHERE id = :userId")
    suspend fun updateUserScore(userId: Long, score: Int)
    
    @Query("SELECT * FROM users WHERE name != :myName AND score BETWEEN :minScore AND :maxScore ORDER BY score DESC LIMIT :limit")
    suspend fun getSimilarUsers(myName: String, minScore: Int, maxScore: Int, limit: Int): List<User>
    
    @Query("DELETE FROM users WHERE name != '파워킹유저'")
    suspend fun deleteAllSampleUsers()
}

