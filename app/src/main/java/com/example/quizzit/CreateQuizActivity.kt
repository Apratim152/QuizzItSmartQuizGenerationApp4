package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.data.entity.QuizEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreateQuizActivity : AppCompatActivity() {

    private lateinit var db: QuizDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        db = QuizDatabase.getDatabase(this)

        val username = intent.getStringExtra("USERNAME") ?: "User"

        // Find all views
        val etTopic = findViewById<TextInputEditText>(R.id.etTopic)
        val etSubject = findViewById<TextInputEditText>(R.id.etSubject)
        val spinnerDifficulty = findViewById<Spinner>(R.id.spinnerDifficulty)
        val etNumQuestions = findViewById<TextInputEditText>(R.id.etNumQuestions)
        val spinnerQuizType = findViewById<Spinner>(R.id.spinnerQuizType)
        val spinnerQuizFormat = findViewById<Spinner>(R.id.spinnerQuizFormat)
        val btnGenerateQuiz = findViewById<MaterialButton>(R.id.btnGenerateQuiz)

        // Populate Spinners
        spinnerDifficulty.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Easy", "Medium", "Hard")
        )
        spinnerQuizType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("MCQ", "True/False", "Short Answer")
        )
        spinnerQuizFormat.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Timed", "Untimed")
        )

        // Generate Quiz Button click
        btnGenerateQuiz.setOnClickListener {
            val topic = etTopic.text.toString().trim()
            val subject = etSubject.text.toString().trim()
            val numQuestions = etNumQuestions.text.toString().toIntOrNull() ?: 0
            val difficulty = spinnerDifficulty.selectedItem.toString()
            val quizType = spinnerQuizType.selectedItem.toString()
            val quizFormat = spinnerQuizFormat.selectedItem.toString()

            if (topic.isEmpty() || subject.isEmpty() || numQuestions <= 0) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Create QuizEntity
                val newQuiz = QuizEntity(
                    title = topic,
                    subject = subject,
                    difficulty = difficulty,
                    totalQuestions = numQuestions,
                    quizType = quizType,
                    format = quizFormat
                )

                // Insert quiz and get its ID
                val quizId = db.quizDao().insertQuiz(newQuiz).toInt()

                // Create placeholder questions
                val questions = (1..numQuestions).map { index ->
                    QuestionEntity(
                        quizOwnerId = quizId,
                        questionText = "Question $index placeholder",
                        optionA = "Option A",
                        optionB = "Option B",
                        optionC = "Option C",
                        optionD = "Option D",
                        correctOption = "Option A"
                    )
                }

                // Insert questions into DB
                db.questionDao().insertQuestions(questions)

                // Start QuizTakingActivity after DB inserts
                val intent = Intent(this@CreateQuizActivity, QuizTakingActivity::class.java).apply {
                    putExtra("quizId", quizId)
                    putExtra("USERNAME", username)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
