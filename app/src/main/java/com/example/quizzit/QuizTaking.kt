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
    private var score = 0
    private var quizId = 0
    private var username: String? = null
    private var totalQuestions = 5

    // ✅ Timer variables - Now for TOTAL quiz time
    private var countDownTimer: CountDownTimer? = null
    private var totalQuizTimeMillis: Long = 0 // 0 means no time limit
    private var timeLeftMillis: Long = 0
    private var hasTimeLimit = false

    // ✅ Prevent multiple clicks
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuiztakingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)

        // Get intent data
        quizId = intent.getIntExtra("quizId", 0)
        username = intent.getStringExtra("USERNAME") ?: "User"
        totalQuestions = intent.getIntExtra("totalQuestions", 5)
        val quizTitle = intent.getStringExtra("quizTitle") ?: "Quiz"

        // ✅ Get total time in minutes and convert to milliseconds
        val totalTimeMinutes = intent.getIntExtra("totalTimeMinutes", 0)
        totalQuizTimeMillis = if (totalTimeMinutes > 0) {
            totalTimeMinutes * 60 * 1000L
        } else {
            0L // No time limit
        }
        hasTimeLimit = totalQuizTimeMillis > 0

        // Set quiz title
        binding.tvQuizTitle.text = quizTitle

        // ✅ Setup back press handler
        setupBackPressHandler()

        // Load questions and start
        lifecycleScope.launch {
            loadQuestions()

            if (questionList.isEmpty()) {
                Toast.makeText(
                    this@QuizTakingActivity,
                    "No questions available",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            showQuestion(currentQuestionIndex)

            // Start timer only if time limit is set
            if (hasTimeLimit) {
                startTimer()
            } else {
                // Hide timer if no time limit
                binding.tvTimer.visibility = View.GONE
            }
        }

        // ✅ Next button with validation and debouncing
        binding.btnNextQuestion.setOnClickListener {
            if (isProcessing) return@setOnClickListener

            // Validate selection
            if (binding.rgOptions.checkedRadioButtonId == -1) {
                Toast.makeText(this, "⚠️ Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isProcessing = true

            checkAnswer()

            if (currentQuestionIndex < questionList.size - 1) {
                currentQuestionIndex++
                showQuestion(currentQuestionIndex)
            } else {
                goToResult()
            }

            // Reset debounce flag after a short delay
            binding.btnNextQuestion.postDelayed({
                isProcessing = false
            }, 300)
        }

        // ✅ Submit button with confirmation
        binding.btnSubmit.setOnClickListener {
            if (isProcessing) return@setOnClickListener

            // Validate selection for last question
            if (binding.rgOptions.checkedRadioButtonId == -1) {
                Toast.makeText(this, "⚠️ Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Submit Quiz?")
                .setMessage("Are you sure you want to submit the quiz? You cannot change your answers after submission.")
                .setPositiveButton("Submit") { _, _ ->
                    isProcessing = true
                    checkAnswer()
                    goToResult()
                }
                .setNegativeButton("Cancel", null)
                .show()
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
                    questionText = "Sample Question $index: What is the correct answer?",
                    optionA = "Option A",
                    optionB = "Option B",
                    optionC = "Option C",
                    optionD = "Option D",
                    correctOption = "Option A"
                )
            }
        }
    }

    private fun showQuestion(index: Int) {
        val question = questionList.getOrNull(index) ?: return

        // Update question number
        binding.tvQuestionNumber.text = "Question ${index + 1} of ${questionList.size}"

        // Update question text
        binding.tvQuestionText.text = question.questionText

        // Set options
        binding.rbOption1.text = question.optionA
        binding.rbOption2.text = question.optionB
        binding.rbOption3.text = question.optionC
        binding.rbOption4.text = question.optionD

        // ✅ Clear selections using RadioGroup
        binding.rgOptions.clearCheck()

        // Update progress bar
        val progress = ((index + 1) * 100) / questionList.size
        binding.progressBar.progress = progress

        // ✅ Show/hide buttons based on position
        val isLastQuestion = index == questionList.size - 1
        binding.btnNextQuestion.visibility = if (isLastQuestion) View.GONE else View.VISIBLE
        binding.btnSubmit.visibility = if (isLastQuestion) View.VISIBLE else View.GONE

        // ✅ Make sure buttons are enabled
        binding.btnNextQuestion.isEnabled = true
        binding.btnSubmit.isEnabled = true
    }

    // ✅ Start countdown timer for TOTAL quiz time
    private fun startTimer() {
        timeLeftMillis = totalQuizTimeMillis

        countDownTimer = object : CountDownTimer(timeLeftMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                // ✅ Time's up - auto submit quiz
                Toast.makeText(
                    this@QuizTakingActivity,
                    "⏰ Time's up! Quiz submitted automatically.",
                    Toast.LENGTH_LONG
                ).show()

                // Check current answer if selected
                if (binding.rgOptions.checkedRadioButtonId != -1) {
                    checkAnswer()
                }

                goToResult()
            }
        }.start()
    }

    // ✅ Update timer display with MM:SS format and color changes
    private fun updateTimerDisplay() {
        val totalSeconds = (timeLeftMillis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)

        // Change color based on remaining time
        binding.tvTimer.setTextColor(
            when {
                totalSeconds > 60 -> getColor(android.R.color.holo_green_dark) // > 1 minute
                totalSeconds > 30 -> getColor(android.R.color.holo_orange_dark) // > 30 seconds
                else -> getColor(android.R.color.holo_red_dark) // < 30 seconds - URGENT!
            }
        )
    }

    private fun checkAnswer() {
        val question = questionList.getOrNull(currentQuestionIndex) ?: return

        val selectedText = when (binding.rgOptions.checkedRadioButtonId) {
            binding.rbOption1.id -> binding.rbOption1.text.toString()
            binding.rbOption2.id -> binding.rbOption2.text.toString()
            binding.rbOption3.id -> binding.rbOption3.text.toString()
            binding.rbOption4.id -> binding.rbOption4.text.toString()
            else -> ""
        }

        if (selectedText == question.correctOption) {
            score++
        }
    }

    private fun goToResult() {
        countDownTimer?.cancel()

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("score", score)
            putExtra("totalQuestions", questionList.size)
            putExtra("USERNAME", username)
            putExtra("quizId", quizId)
        }
        startActivity(intent)
        finish()
    }

    // ✅ Handle back press with confirmation using OnBackPressedDispatcher
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@QuizTakingActivity)
                    .setTitle("Exit Quiz?")
                    .setMessage("Your progress will be lost. Are you sure you want to exit?")
                    .setPositiveButton("Exit") { _, _ ->
                        countDownTimer?.cancel()
                        isEnabled = false // Disable callback to allow back press
                        finish()
                    }
                    .setNegativeButton("Continue Quiz", null)
                    .show()
            }
        })
    }

    // ✅ Pause timer when activity goes to background
    override fun onPause() {
        super.onPause()
        countDownTimer?.cancel()
    }

    // ✅ Resume timer when activity comes back
    override fun onResume() {
        super.onResume()
        if (hasTimeLimit && timeLeftMillis > 0 && questionList.isNotEmpty()) {
            // Restart timer with remaining time
            countDownTimer = object : CountDownTimer(timeLeftMillis, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftMillis = millisUntilFinished
                    updateTimerDisplay()
                }

                override fun onFinish() {
                    Toast.makeText(
                        this@QuizTakingActivity,
                        "⏰ Time's up!",
                        Toast.LENGTH_LONG
                    ).show()

                    if (binding.rgOptions.checkedRadioButtonId != -1) {
                        checkAnswer()
                    }
                    goToResult()
                }
            }.start()
        }
    }

    // ✅ Cancel timer when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}