package com.example.quizzit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val quizId: Int = 0,

    val title: String,
    val subject: String,
    val difficulty: String,

    val totalQuestions: Int,
    val quizType: String,     // e.g., "MCQ_SINGLE"
    val format: String,       // e.g., "Timed"

    val createdAt: Long = System.currentTimeMillis()
)
