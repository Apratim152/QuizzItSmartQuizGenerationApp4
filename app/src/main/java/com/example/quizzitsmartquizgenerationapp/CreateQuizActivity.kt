package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CreateQuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        // Receive username from login
        val username = intent.getStringExtra("USERNAME")

        // Show welcome message
        val tvWelcome = TextView(this)
        tvWelcome.text = "Welcome, $username! Create your quiz below."
        tvWelcome.textSize = 20f
        tvWelcome.setPadding(16,16,16,16)

        // Find all views
        val etTopic = findViewById<EditText>(R.id.etTopic)
        val etSubject = findViewById<EditText>(R.id.etSubject)
        val spinnerDifficulty = findViewById<Spinner>(R.id.spinnerDifficulty)
        val etContentUrl = findViewById<EditText>(R.id.etContentUrl)
        val etNumQuestions = findViewById<EditText>(R.id.etNumQuestions)
        val spinnerQuizType = findViewById<Spinner>(R.id.spinnerQuizType)
        val spinnerQuizFormat = findViewById<Spinner>(R.id.spinnerQuizFormat)
        val btnGenerateQuiz = findViewById<Button>(R.id.btnGenerateQuiz)

        // Populate Spinners (example values)
        spinnerDifficulty.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Easy", "Medium", "Hard")
        )

        spinnerQuizType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("MCQ", "True/False", "Short Answer")
        )

        spinnerQuizFormat.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Timed", "Untimed")
        )

        // Generate Quiz Button
        btnGenerateQuiz.setOnClickListener {
            val topic = etTopic.text.toString()
            val subject = etSubject.text.toString()
            val numQuestions = etNumQuestions.text.toString()

            if(topic.isEmpty() || subject.isEmpty() || numQuestions.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            } else {
                // Pass quiz details to QuizTaking activity
                val intent = Intent(this, QuizTaking::class.java)
                intent.putExtra("USERNAME", username)
                intent.putExtra("TOPIC", topic)
                intent.putExtra("SUBJECT", subject)
                intent.putExtra("DIFFICULTY", spinnerDifficulty.selectedItem.toString())
                intent.putExtra("NUM_QUESTIONS", numQuestions)
                intent.putExtra("QUIZ_TYPE", spinnerQuizType.selectedItem.toString())
                intent.putExtra("QUIZ_FORMAT", spinnerQuizFormat.selectedItem.toString())
                startActivity(intent)
            }
        }
    }
}
