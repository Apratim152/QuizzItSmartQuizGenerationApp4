package com.example.quizzit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val questionId: Int = 0,

    val quizOwnerId: Int = 0,  // will be updated when quiz is saved

    val questionText: String = "",

    // Multiple-choice options
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",

    // Correct option (A, B, C, D)
    val correctOption: String = "",

    // Optional: hint provided by API
    val hint: String = ""
)
