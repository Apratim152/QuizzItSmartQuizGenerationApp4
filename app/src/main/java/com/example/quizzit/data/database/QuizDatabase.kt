package com.example.quizzit.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quizzit.data.dao.QuestionDao
import com.example.quizzit.data.dao.QuizDao
import com.example.quizzit.data.dao.ResultDao
import com.example.quizzit.data.dao.UserDao
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.data.entity.QuizEntity
import com.example.quizzit.data.entity.Result
import com.example.quizzit.data.entity.UserEntity

@Database(
    entities = [QuizEntity::class, QuestionEntity::class, Result::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun resultDao(): ResultDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}