package com.example.quizzit

import android.content.Intent
import android.os.Bundle
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
    private var totalQuestions = 5 // default number of dummy questions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuiztakingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)

        quizId = intent.getIntExtra("quizId", 0)
        username = intent.getStringExtra("USERNAME") ?: "User"
        totalQuestions = intent.getIntExtra("totalQuestions", 5)

        // Load questions (dummy placeholders if DB empty)
        lifecycleScope.launch {
            loadQuestions()
            showQuestion(currentQuestionIndex)
        }

        binding.btnNextQuestion.setOnClickListener {
            checkAnswer()
            if (currentQuestionIndex < questionList.size - 1) {
                currentQuestionIndex++
                showQuestion(currentQuestionIndex)
            } else {
                goToResult()
            }
        }

        binding.btnSubmit.setOnClickListener {
            checkAnswer()
            goToResult()
        }
    }

    // Load questions from DB; create dummy if empty
    private suspend fun loadQuestions() {
        val dbQuestions = db.questionDao().getQuestionsByQuizId(quizId)
        questionList = if (dbQuestions.isNotEmpty()) {
            dbQuestions
        } else {
            // Generate dummy questions
            (1..totalQuestions).map { index ->
                QuestionEntity(
                    quizOwnerId = quizId,
                    questionText = "Question $index",
                    optionA = "Option A",
                    optionB = "Option B",
                    optionC = "Option C",
                    optionD = "Option D",
                    correctOption = "Option A" // matches text
                )
            }
        }
    }

    private fun showQuestion(index: Int) {
        val question = questionList.getOrNull(index) ?: return

        binding.tvQuestionNumber.text = "Question ${index + 1} of ${questionList.size}"
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
        val question = questionList.getOrNull(currentQuestionIndex) ?: return
        val selectedText = when {
            binding.rbOption1.isChecked -> binding.rbOption1.text.toString()
            binding.rbOption2.isChecked -> binding.rbOption2.text.toString()
            binding.rbOption3.isChecked -> binding.rbOption3.text.toString()
            binding.rbOption4.isChecked -> binding.rbOption4.text.toString()
            else -> ""
        }
        if (selectedText == question.correctOption) score++
    }

    private fun goToResult() {
        val intent = Intent(this, ResultActivity::class.java).apply {  // Changed from Result::class.java
            putExtra("score", score)
            putExtra("totalQuestions", questionList.size)
            putExtra("USERNAME", username)
            putExtra("quizId", quizId)
        }
        startActivity(intent)
        finish()
    }
}
