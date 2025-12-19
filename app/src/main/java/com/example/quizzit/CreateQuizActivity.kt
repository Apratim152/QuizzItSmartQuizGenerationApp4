package com.example.quizzit

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzit.api.QuizRequest
import com.example.quizzit.api.QuizService
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.data.entity.QuizEntity
import com.example.quizzit.ui.GeneratedQuestionsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CreateQuizActivity : AppCompatActivity() {

    private lateinit var etQuizTitle: EditText
    private lateinit var etTopicOrPDF: EditText
    private lateinit var btnGenerateQuestions: Button
    private lateinit var btnSaveQuiz: Button
    private lateinit var rvGeneratedQuestions: RecyclerView

    private val generatedQuestions = mutableListOf<QuestionEntity>()
    private lateinit var adapter: GeneratedQuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        etQuizTitle = findViewById(R.id.etQuizTitle)
        etTopicOrPDF = findViewById(R.id.etTopicOrPDF)
        btnGenerateQuestions = findViewById(R.id.btnGenerateQuestions)
        btnSaveQuiz = findViewById(R.id.btnSaveQuiz)
        rvGeneratedQuestions = findViewById(R.id.rvGeneratedQuestions)

        adapter = GeneratedQuestionsAdapter(generatedQuestions)
        rvGeneratedQuestions.layoutManager = LinearLayoutManager(this)
        rvGeneratedQuestions.adapter = adapter

        btnGenerateQuestions.setOnClickListener {
            val topicText = etTopicOrPDF.text.toString().trim()
            if (topicText.isEmpty()) {
                Toast.makeText(this, "Enter a topic or PDF URL", Toast.LENGTH_SHORT).show()
            } else {
                generateQuestions(topicText)
            }
        }

        btnSaveQuiz.setOnClickListener {
            saveQuizToDatabase()
        }
    }

    private fun generateQuestions(topic: String) {
        lifecycleScope.launch {
            try {
                generatedQuestions.clear()
                adapter.notifyDataSetChanged()

                val response = QuizService.quizApi.generateQuiz(
                    QuizRequest(topic, 5)
                )

                if (response.isSuccessful) {
                    response.body()?.cards?.forEach { card ->
                        generatedQuestions.add(
                            QuestionEntity(
                                quizOwnerId = 0,
                                questionText = card.question,
                                optionA = card.choices.getOrElse(0) { "" },
                                optionB = card.choices.getOrElse(1) { "" },
                                optionC = card.choices.getOrElse(2) { "" },
                                optionD = card.choices.getOrElse(3) { "" },
                                correctOption = listOf("A", "B", "C", "D")
                                    .getOrElse(card.correct_index) { "" }
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@CreateQuizActivity,
                        "API Error ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@CreateQuizActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveQuizToDatabase() {
        val quizTitle = etQuizTitle.text.toString().trim()

        if (quizTitle.isEmpty() || generatedQuestions.isEmpty()) {
            Toast.makeText(this, "Fill details and generate questions", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = QuizDatabase.getDatabase(this@CreateQuizActivity)

            val quiz = QuizEntity(
                title = quizTitle,
                subject = etTopicOrPDF.text.toString(),
                difficulty = "",
                totalQuestions = generatedQuestions.size,
                quizType = "",
                format = ""
            )

            val quizId = withContext(Dispatchers.IO) {
                db.quizDao().insertQuiz(quiz).toInt()
            }

            withContext(Dispatchers.IO) {
                db.questionDao().insertQuestions(
                    generatedQuestions.map { it.copy(quizOwnerId = quizId) }
                )
            }

            Toast.makeText(this@CreateQuizActivity, "Quiz Saved!", Toast.LENGTH_SHORT).show()
            generatedQuestions.clear()
            adapter.notifyDataSetChanged()
        }
    }
}
