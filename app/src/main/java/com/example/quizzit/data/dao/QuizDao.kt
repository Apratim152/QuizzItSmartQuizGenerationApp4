package com.example.quizzit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quizzit.data.entity.QuizEntity

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long  // returns auto-generated ID

    @Query("SELECT * FROM quizzes ORDER BY quizId DESC")
    suspend fun getAllQuizzes(): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: Int): QuizEntity?
}
