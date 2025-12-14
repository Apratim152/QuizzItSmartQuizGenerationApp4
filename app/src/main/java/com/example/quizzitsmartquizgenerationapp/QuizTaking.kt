package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class QuizTaking : AppCompatActivity() {

    private var currentQuestionIndex = 0
    private val userAnswers = mutableListOf<String>()
    private lateinit var questions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiztaking)

        val username = intent.getStringExtra("USERNAME")
        val topic = intent.getStringExtra("TOPIC") ?: "General"
        val numQuestions = intent.getStringExtra("NUM_QUESTIONS")?.toIntOrNull() ?: 3

        questions = List(numQuestions) { index -> "$topic Question ${index + 1}" }

        val tvQuizTitle = findViewById<TextView>(R.id.tvQuizTitle)
        val tvQuestionNumber = findViewById<TextView>(R.id.tvQuestionNumber)
        val tvQuestionText = findViewById<TextView>(R.id.tvQuestionText)
        val rbOption1 = findViewById<RadioButton>(R.id.rbOption1)
        val rbOption2 = findViewById<RadioButton>(R.id.rbOption2)
        val rbOption3 = findViewById<RadioButton>(R.id.rbOption3)
        val rbOption4 = findViewById<RadioButton>(R.id.rbOption4)
        val btnNextQuestion = findViewById<Button>(R.id.btnNextQuestion)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        tvQuizTitle.text = "$topic Quiz"

        fun showQuestion(index: Int) {
            tvQuestionNumber.text = "Question ${index + 1} of $numQuestions"
            tvQuestionText.text = questions[index]
            rbOption1.isChecked = false
            rbOption2.isChecked = false
            rbOption3.isChecked = false
            rbOption4.isChecked = false
            rbOption1.text = "Option A"
            rbOption2.text = "Option B"
            rbOption3.text = "Option C"
            rbOption4.text = "Option D"
        }

        showQuestion(currentQuestionIndex)

        btnNextQuestion.setOnClickListener {
            val selectedOption = when {
                rbOption1.isChecked -> rbOption1.text.toString()
                rbOption2.isChecked -> rbOption2.text.toString()
                rbOption3.isChecked -> rbOption3.text.toString()
                rbOption4.isChecked -> rbOption4.text.toString()
                else -> ""
            }

            if (selectedOption.isEmpty()) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userAnswers.add(selectedOption)
            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                showQuestion(currentQuestionIndex)
            } else {
                val intent = Intent(this, Result::class.java)
                intent.putExtra("USERNAME", username)
                intent.putExtra("TOPIC", topic)
                intent.putStringArrayListExtra("USER_ANSWERS", ArrayList(userAnswers))
                startActivity(intent)
                finish()
            }
        }

        btnSubmit.setOnClickListener {
            val selectedOption = when {
                rbOption1.isChecked -> rbOption1.text.toString()
                rbOption2.isChecked -> rbOption2.text.toString()
                rbOption3.isChecked -> rbOption3.text.toString()
                rbOption4.isChecked -> rbOption4.text.toString()
                else -> ""
            }

            if (selectedOption.isNotEmpty()) {
                userAnswers.add(selectedOption)
            }

            val intent = Intent(this, Result::class.java)
            intent.putExtra("USERNAME", username)
            intent.putExtra("TOPIC", topic)
            intent.putStringArrayListExtra("USER_ANSWERS", ArrayList(userAnswers))
            startActivity(intent)
            finish()
        }
    }
}
