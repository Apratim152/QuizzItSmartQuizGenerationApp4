package com.example.quizzit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,

    val username: String,
    val email: String,

    // Stats for leaderboard
    val totalXP: Int = 0,
    val quizzesCompleted: Int = 0,
    val totalScore: Int = 0,
    val averageScore: Float = 0f,
    val highestScore: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)