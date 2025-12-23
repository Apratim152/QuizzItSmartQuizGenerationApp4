package com.example.quizzit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quizzit.data.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY totalXP DESC")
    suspend fun getAllUsersByXP(): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY averageScore DESC")
    suspend fun getAllUsersByAverageScore(): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY quizzesCompleted DESC")
    suspend fun getAllUsersByQuizzesCompleted(): List<UserEntity>

    @Query("UPDATE users SET totalXP = totalXP + :xp, quizzesCompleted = quizzesCompleted + 1, totalScore = totalScore + :score WHERE username = :username")
    suspend fun updateUserStats(username: String, xp: Int, score: Int)
}