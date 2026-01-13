package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.databinding.ActivityResultBinding
import com.example.quizzit.data.entity.Result
import com.example.quizzit.firebase.FirebaseUserRepository
import com.example.quizzit.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var db: QuizDatabase

    private var quizId = 0
    private var score = 0
    private var total = 0
    private var xpEarned = 0
    private var username: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)
        PreferenceManager.init(this)

        // Get data from Intent
        quizId = intent.getIntExtra("quizId", 0)
        score = intent.getIntExtra("score", 0)
        total = intent.getIntExtra("totalQuestions", 0)
        username = intent.getStringExtra("USERNAME") ?: "User"

        // Calculate XP based on score
        xpEarned = score * 10

        Log.d("ResultActivity", "Quiz Results - Score: $score/$total, XP: $xpEarned, Username: $username")

        // Display results
        displayResults()

        // Save result to database AND update user stats
        saveResultAndUpdateStats()

        // Setup buttons
        setupButtons()

        // Handle back button with OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })
    }

    private fun displayResults() {
        // Calculate percentage
        val percentage = if (total > 0) (score * 100) / total else 0

        // Set username
        binding.usernameTextView.text = "User: $username"

        // Set score
        binding.scoreTextView.text = "$score / $total"

        // Set percentage
        binding.percentageTextView.text = "$percentage%"

        // Set result message with emoji based on performance
        binding.resultMessageTextView.text = when {
            percentage >= 90 -> "🎉 Excellent! Outstanding performance!"
            percentage >= 75 -> "👍 Great Job! Well done!"
            percentage >= 60 -> "🙂 Good Effort! Keep it up!"
            percentage >= 40 -> "📚 Not bad! Keep practicing!"
            else -> "💪 Keep Practicing! You can do better!"
        }

        // Set text color based on performance
        val colorRes = when {
            percentage >= 75 -> android.R.color.holo_green_dark
            percentage >= 50 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_red_dark
        }
        binding.percentageTextView.setTextColor(getColor(colorRes))
    }

    private fun saveResultAndUpdateStats() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 1. Save result to results table
                    val result = Result(
                        quizId = quizId,
                        score = score,
                        total = total,
                        xpEarned = xpEarned,
                        timestamp = System.currentTimeMillis()
                    )
                    db.resultDao().insertResult(result)
                    Log.d("ResultActivity", "Result saved to database")

                    // 2. Update user stats locally
                    val user = db.userDao().getUserByUsername(username)
                    if (user == null) {
                        Log.e("ResultActivity", "ERROR: User '$username' not found!")
                        return@withContext
                    }

                    Log.d("ResultActivity", "User before update: $user")

                    // Update XP, quizzes completed, and total score
                    db.userDao().updateUserStats(username, xpEarned, score)

                    // Get updated user and calculate average/highest
                    val updatedUser = db.userDao().getUserByUsername(username)
                    if (updatedUser != null) {
                        val newAverageScore = if (updatedUser.quizzesCompleted > 0) {
                            updatedUser.totalScore.toFloat() / updatedUser.quizzesCompleted
                        } else {
                            0f
                        }
                        val newHighestScore = maxOf(updatedUser.highestScore, score)

                        // Update the user entity with calculated values
                        val finalUser = updatedUser.copy(
                            averageScore = newAverageScore,
                            highestScore = newHighestScore
                        )
                        db.userDao().updateUser(finalUser)

                        Log.d("ResultActivity", "User after update: $finalUser")
                    }

                    Log.d("ResultActivity", "✅ Stats updated successfully!")

                    // 3. ✅ Sync to Firebase Realtime Database
                    try {
                        val firebaseUid = PreferenceManager.getFirebaseUid()
                        Log.d("Firebase", "Firebase UID: $firebaseUid")

                        if (firebaseUid.isNotEmpty()) {
                            val fbUserRepo = FirebaseUserRepository()

                            fbUserRepo.updateUserStats(
                                userId = firebaseUid,
                                xpEarned = xpEarned,
                                score = score,
                                quizzesCompleted = 1
                            )
                                .onSuccess {
                                    Log.d("Firebase", "✅ Firebase leaderboard synced! XP: $xpEarned")
                                }
                                .onFailure { error ->
                                    Log.e("Firebase", "Error syncing to Firebase: ${error.message}")
                                    error.printStackTrace()
                                }
                        } else {
                            Log.w("Firebase", "No Firebase UID found - user may not be authenticated")
                        }
                    } catch (e: Exception) {
                        Log.e("Firebase", "Exception syncing to Firebase: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // Show success message
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ResultActivity,
                        "🎉 +$xpEarned XP earned!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("ResultActivity", "❌ Error saving result: ${e.message}", e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ResultActivity,
                        "Error updating stats",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupButtons() {
        // Home button - return to main activity
        binding.homeButton.setOnClickListener {
            navigateToHome()
        }

        // Retake button - restart the same quiz
        binding.retakeButton.setOnClickListener {
            val intent = Intent(this, QuizTakingActivity::class.java).apply {
                putExtra("USERNAME", username)
                putExtra("quizId", quizId)
                putExtra("totalQuestions", total)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("USERNAME", username)
        startActivity(intent)
        finish()
    }
}