package com.example.quizzit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quizzit.data.entity.QuestionEntity

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    // ✅ Add this function to fetch questions by quiz ID
    @Query("SELECT * FROM questions WHERE quizOwnerId = :quizId")
    suspend fun getQuestionsByQuizId(quizId: Int): List<QuestionEntity>
}
