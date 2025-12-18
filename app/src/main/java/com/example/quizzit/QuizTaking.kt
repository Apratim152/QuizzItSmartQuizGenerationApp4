package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzit.databinding.ActivityQuiztakingBinding
import com.example.quizzit.data.entity.QuestionEntity

class QuizTakingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuiztakingBinding

    private var questionList: MutableList<QuestionEntity> = mutableListOf()
    private var currentQuestionIndex = 0
    private var score = 0
    private var quizId = 0
    private var username: String? = null
    private var totalQuestions = 5 // default placeholder questions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuiztakingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve extras
        quizId = intent.getIntExtra("quizId", 0)
        username = intent.getStringExtra("USERNAME")

        // Create placeholder questions if DB is empty / not yet integrated
        generatePlaceholderQuestions()

        // Display first question
        showQuestion()

        // Next question button
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

    private fun generatePlaceholderQuestions() {
        questionList.clear()
        for (i in 1..totalQuestions) {
            questionList.add(
                QuestionEntity(
                    questionId = i,
                    quizOwnerId = quizId,
                    questionText = "Question $i placeholder",
                    optionA = "Option A",
                    optionB = "Option B",
                    optionC = "Option C",
                    optionD = "Option D",
                    correctOption = "A" // arbitrary correct answer for now
                )
            )
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

        // Clear previous selection
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
