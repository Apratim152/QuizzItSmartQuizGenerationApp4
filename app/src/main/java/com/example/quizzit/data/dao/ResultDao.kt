package com.example.quizzit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quizzit.data.entity.ResultEntity

@Dao
interface ResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity)

    @Query("SELECT * FROM results ORDER BY resultId DESC")
    suspend fun getAllResults(): List<ResultEntity>

    @Query("SELECT * FROM results WHERE quizId = :quizId LIMIT 1")
    suspend fun getResultByQuizId(quizId: Int): ResultEntity?
}
