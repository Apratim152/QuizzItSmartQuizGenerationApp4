package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var username: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get username from intent if passed from login
        username = intent.getStringExtra("USERNAME") ?: "User"

        // Navigate to QuizGenerationActivity to create AI-powered quiz
        binding.btnCreateQuiz.setOnClickListener {
            val intent = Intent(this, QuizGenerationActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Show confirmation dialog before exiting
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit QuizzIt")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                super.onBackPressed()
                finishAffinity() // Close all activities
            }
            .setNegativeButton("No", null)
            .show()
    }
}