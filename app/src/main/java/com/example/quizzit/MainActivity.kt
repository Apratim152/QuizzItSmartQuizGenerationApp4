package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzit.databinding.ActivityMainBinding
import com.example.quizzit.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var username: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize PreferenceManager
        PreferenceManager.init(this)

        // Get username from intent or SharedPreferences
        username = intent.getStringExtra("USERNAME") ?: PreferenceManager.getUsername()
        if (username.isEmpty()) {
            username = "User"
        }

        // Display welcome message
        binding.welcomeTextView.text = "Welcome, $username! 👋"

        // Navigate to QuizGenerationActivity to create AI-powered quiz
        binding.btnCreateQuiz.setOnClickListener {
            val intent = Intent(this, QuizGenerationActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        // ✅ Navigate to Quiz History
        binding.btnViewHistory.setOnClickListener {
            val intent = Intent(this, QuizHistoryActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        // Navigate to Leaderboard
        binding.btnLeaderboard.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        // Handle back button with OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Show logout confirmation dialog
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { _, _ ->
                        logout()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        })
    }

    private fun logout() {
        // Clear SharedPreferences
        PreferenceManager.logout()

        // Redirect to LoginCredentials
        val intent = Intent(this, LoginCredentials::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}