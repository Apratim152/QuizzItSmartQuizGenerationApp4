package com.example.quizzit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class Result(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val quizId: Int,
    val score: Int,
    val total: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)