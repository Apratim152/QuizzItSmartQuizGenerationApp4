package com.example.quizzit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey(autoGenerate = true)
    val resultId: Int = 0,  // primary key

    val quizId: Int,         // links to QuizEntity.quizId
    val score: Int,
    val total: Int,
    val xpEarned: Int,
    val attemptedAt: Long = System.currentTimeMillis()
)
