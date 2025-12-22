package com.example.quizzit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quizzit.data.entity.Result

@Dao
interface ResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: Result)

    @Query("SELECT * FROM results ORDER BY id DESC")
    suspend fun getAllResults(): List<Result>

    @Query("SELECT * FROM results WHERE quizId = :quizId LIMIT 1")
    suspend fun getResultByQuizId(quizId: Int): Result?
}