package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var btnCreateQuiz: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCreateQuiz = findViewById(R.id.btnCreateQuiz)

        // Navigate to QuizGenerationActivity to create AI-powered quiz
        btnCreateQuiz.setOnClickListener {
            val intent = Intent(this, QuizGenerationActivity::class.java)
            intent.putExtra("USERNAME", "User") // Pass username if needed
            startActivity(intent)
        }
    }
}