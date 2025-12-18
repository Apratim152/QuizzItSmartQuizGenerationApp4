package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.databinding.ActivityQuiztakingBinding
import kotlinx.coroutines.launch

class QuizTakingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuiztakingBinding
    private lateinit var db: QuizDatabase
    private var questionList: List<QuestionEntity> = emptyList()
    private var currentQuestionIndex = 0
    private var score = 0
    private var quizId = 0
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuiztakingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)

        // Retrieve extras
        quizId = intent.getIntExtra("quizId", 0)
        username = intent.getStringExtra("USERNAME")

        if (quizId == 0 || username.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid quiz session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load questions from DB
        lifecycleScope.launch {
            questionList = db.questionDao(). getQuestionsByQuizId(quizId)
            if (questionList.isEmpty()) {
                Toast.makeText(this@QuizTakingActivity, "No questions found!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                showQuestion()
            }
        }

        // Next button
        binding.btnNextQuestion.setOnClickListener {
            checkAnswer()
            if (currentQuestionIndex < questionList.size - 1) {
                currentQuestionIndex++
                showQuestion()
            } else {
                goToResultActivity()
            }
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            checkAnswer()
            goToResultActivity()
        }
    }

    private fun showQuestion() {
        val question = questionList[currentQuestionIndex]
        binding.tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${questionList.size}"
        binding.tvQuestionText.text = question.questionText
        binding.rbOption1.text = question.optionA
        binding.rbOption2.text = question.optionB
        binding.rbOption3.text = question.optionC
        binding.rbOption4.text = question.optionD

        binding.rbOption1.isChecked = false
        binding.rbOption2.isChecked = false
        binding.rbOption3.isChecked = false
        binding.rbOption4.isChecked = false
    }

    private fun checkAnswer() {
        val question = questionList[currentQuestionIndex]
        val selected = when {
            binding.rbOption1.isChecked -> "A"
            binding.rbOption2.isChecked -> "B"
            binding.rbOption3.isChecked -> "C"
            binding.rbOption4.isChecked -> "D"
            else -> ""
        }
        if (selected == question.correctOption) score++
    }

    private fun goToResultActivity() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("score", score)
            putExtra("totalQuestions", questionList.size)
            putExtra("USERNAME", username)
            putExtra("quizId", quizId)
        }
        startActivity(intent)
        finish()
    }
}
