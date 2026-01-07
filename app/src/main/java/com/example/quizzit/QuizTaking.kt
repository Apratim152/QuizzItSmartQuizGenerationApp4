package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
    private var quizId = 0
    private var username: String? = null
    private var totalQuestions = 5
    private var score = 0

    // ✅ store answers
    private val userAnswers = mutableMapOf<Int, String>()

    // Timer
    private var countDownTimer: CountDownTimer? = null
    private var totalQuizTimeMillis: Long = 0
    private var timeLeftMillis: Long = 0
    private var hasTimeLimit = false

    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuiztakingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)

        quizId = intent.getIntExtra("quizId", 0)
        username = intent.getStringExtra("USERNAME") ?: "User"
        totalQuestions = intent.getIntExtra("totalQuestions", 5)
        val quizTitle = intent.getStringExtra("quizTitle") ?: "Quiz"

        val totalTimeMinutes = intent.getIntExtra("totalTimeMinutes", 0)
        totalQuizTimeMillis = if (totalTimeMinutes > 0) totalTimeMinutes * 60 * 1000L else 0L
        hasTimeLimit = totalQuizTimeMillis > 0

        binding.tvQuizTitle.text = quizTitle
        setupBackPressHandler()

        lifecycleScope.launch {
            loadQuestions()
            if (questionList.isEmpty()) {
                Toast.makeText(this@QuizTakingActivity, "No questions available", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            showQuestion(0)

            if (hasTimeLimit) startTimer() else binding.tvTimer.visibility = View.GONE
        }

        // NEXT
        binding.btnNextQuestion.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            if (!saveAnswer()) return@setOnClickListener

            isProcessing = true
            currentQuestionIndex++
            showQuestion(currentQuestionIndex)

            binding.btnNextQuestion.postDelayed({ isProcessing = false }, 300)
        }

        // PREVIOUS
        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                saveAnswer()
                currentQuestionIndex--
                showQuestion(currentQuestionIndex)
            }
        }

        // SUBMIT
        binding.btnSubmit.setOnClickListener {
            if (!saveAnswer()) return@setOnClickListener
            calculateScore()
            goToResult()
        }
    }

    private suspend fun loadQuestions() {
        val dbQuestions = db.questionDao().getQuestionsByQuizId(quizId)
        questionList = if (dbQuestions.isNotEmpty()) dbQuestions else
            (1..totalQuestions).map {
                QuestionEntity(
                    quizOwnerId = quizId,
                    questionText = "Sample Question $it",
                    optionA = "Option A",
                    optionB = "Option B",
                    optionC = "Option C",
                    optionD = "Option D",
                    correctOption = "Option A"
                )
            }
    }

    private fun showQuestion(index: Int) {
        val q = questionList[index]

        binding.tvQuestionNumber.text = "Question ${index + 1} of ${questionList.size}"
        binding.tvQuestionText.text = q.questionText

        // Set option text
        binding.rbOption1.text = q.optionA
        binding.rbOption2.text = q.optionB
        binding.rbOption3.text = q.optionC
        binding.rbOption4.text = q.optionD

        binding.rgOptions.clearCheck()

        // restore answer
        userAnswers[index]?.let {
            when (it) {
                q.optionA -> binding.rbOption1.isChecked = true
                q.optionB -> binding.rbOption2.isChecked = true
                q.optionC -> binding.rbOption3.isChecked = true
                q.optionD -> binding.rbOption4.isChecked = true
            }
        }

        binding.progressBar.progress = ((index + 1) * 100) / questionList.size

        binding.btnPrevious.visibility = if (index == 0) View.GONE else View.VISIBLE
        binding.btnNextQuestion.visibility = if (index == questionList.lastIndex) View.GONE else View.VISIBLE
        binding.btnSubmit.visibility = if (index == questionList.lastIndex) View.VISIBLE else View.GONE
    }

    private fun saveAnswer(): Boolean {
        val selected = when (binding.rgOptions.checkedRadioButtonId) {
            binding.rbOption1.id -> binding.rbOption1.text.toString()
            binding.rbOption2.id -> binding.rbOption2.text.toString()
            binding.rbOption3.id -> binding.rbOption3.text.toString()
            binding.rbOption4.id -> binding.rbOption4.text.toString()
            else -> null
        }

        if (selected == null) {
            Toast.makeText(this, "⚠️ Please select an answer", Toast.LENGTH_SHORT).show()
            return false
        }

        userAnswers[currentQuestionIndex] = selected
        return true
    }

    private fun calculateScore() {
        score = 0
        questionList.forEachIndexed { index, q ->
            if (userAnswers[index] == q.correctOption) score++
        }
    }

    private fun startTimer() {
        timeLeftMillis = totalQuizTimeMillis
        countDownTimer = object : CountDownTimer(timeLeftMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                val s = (millisUntilFinished / 1000).toInt()
                binding.tvTimer.text = String.format("%02d:%02d", s / 60, s % 60)
            }

            override fun onFinish() {
                calculateScore()
                goToResult()
            }
        }.start()
    }

    private fun goToResult() {
        countDownTimer?.cancel()
        startActivity(
            Intent(this, ResultActivity::class.java).apply {
                putExtra("score", score)
                putExtra("totalQuestions", questionList.size)
                putExtra("USERNAME", username)
            }
        )
        finish()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@QuizTakingActivity)
                    .setTitle("Exit Quiz?")
                    .setMessage("Your progress will be lost.")
                    .setPositiveButton("Exit") { _, _ -> finish() }
                    .setNegativeButton("Stay", null)
                    .show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}