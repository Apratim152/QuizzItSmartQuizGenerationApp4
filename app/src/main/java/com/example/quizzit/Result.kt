package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.ResultEntity
import com.example.quizzit.databinding.ActivityResultBinding
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var db: QuizDatabase
    private var quizId = 0
    private var score = 0
    private var total = 0
    private var xpEarned = 0
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)

        // Receive extras
        quizId = intent.getIntExtra("quizId", 0)
        score = intent.getIntExtra("score", 0)
        total = intent.getIntExtra("totalQuestions", 0)
        username = intent.getStringExtra("USERNAME") ?: "User"
        xpEarned = score * 10

        // Set UI
        binding.usernameTextView.text = "User: $username"
        binding.scoreTextView.text = "$score / $total"
        binding.percentageTextView.text = if (total > 0) "${(score * 100) / total}%" else "0%"

        binding.resultMessageTextView.text = when {
            total > 0 && (score * 100) / total >= 90 -> "Excellent! 🎉"
            total > 0 && (score * 100) / total >= 75 -> "Great Job! 👍"
            total > 0 && (score * 100) / total >= 50 -> "Good Effort! 🙂"
            else -> "Keep Practicing! 💪"
        }

        // Save result
        lifecycleScope.launch {
            val result = ResultEntity(
                quizId = quizId, score = score, total = total, xpEarned = xpEarned
            )
            db.resultDao().insertResult(result)
        }

        // Button actions
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.retakeButton.setOnClickListener {
            val intent = Intent(this, QuizTakingActivity::class.java)
            intent.putExtra("USERNAME", username)
            intent.putExtra("quizId", quizId)
            startActivity(intent)
            finish()
        }
    }
}
