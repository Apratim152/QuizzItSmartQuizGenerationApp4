package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Result : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val username = intent.getStringExtra("USERNAME") ?: "User"
        val topic = intent.getStringExtra("TOPIC") ?: "General"
        val userAnswers = intent.getStringArrayListExtra("USER_ANSWERS") ?: arrayListOf()

        // Simulated correct answers (for demonstration)
        val correctAnswers = List(userAnswers.size) { "Option A" } // replace with actual logic if needed

        // Calculate score
        var score = 0
        for (i in userAnswers.indices) {
            if (userAnswers[i] == correctAnswers[i]) score++
        }
        val percentage = (score * 100) / correctAnswers.size

        // Find views
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        val percentageTextView = findViewById<TextView>(R.id.percentageTextView)
        val resultMessageTextView = findViewById<TextView>(R.id.resultMessageTextView)
        val homeButton = findViewById<MaterialButton>(R.id.homeButton)
        val retakeButton = findViewById<MaterialButton>(R.id.retakeButton)

        // Set values
        usernameTextView.text = "User: $username"
        scoreTextView.text = "Score: $score / ${correctAnswers.size}"
        percentageTextView.text = "$percentage%"
        resultMessageTextView.text = when {
            percentage >= 80 -> "Excellent Job!"
            percentage >= 50 -> "Good Effort!"
            else -> "Keep Practicing!"
        }

        // Button actions
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        retakeButton.setOnClickListener {
            val intent = Intent(this, CreateQuizActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
