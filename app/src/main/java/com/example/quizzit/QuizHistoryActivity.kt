package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzit.ui.QuizHistoryAdapter
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuizEntity
import com.example.quizzit.databinding.ActivityQuizHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizHistoryBinding
    private lateinit var db: QuizDatabase
    private lateinit var adapter: QuizHistoryAdapter
    private var quizList = mutableListOf<QuizEntity>()
    private var username: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: "User"

        Log.d("QuizHistory", "Activity created, username: $username")
        setupUI()
        loadQuizzes()
    }

    private fun setupUI() {
        // Setup toolbar/back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = QuizHistoryAdapter(quizList) { quiz ->
            onQuizItemClick(quiz)
        }

        binding.rvQuizzes.apply {
            layoutManager = LinearLayoutManager(this@QuizHistoryActivity)
            adapter = this@QuizHistoryActivity.adapter
        }
    }

    private fun loadQuizzes() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvEmptyState.visibility = android.view.View.GONE

        lifecycleScope.launch {
            try {
                val quizzes = withContext(Dispatchers.IO) {
                    db.quizDao().getAllQuizzes()
                }

                Log.d("QuizHistory", "Loaded ${quizzes.size} quizzes")

                quizList.clear()
                quizList.addAll(quizzes.sortedByDescending { it.createdAt })

                adapter.notifyDataSetChanged()

                if (quizList.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                    binding.tvEmptyState.text = "No quizzes yet.\nCreate one to get started!"
                    binding.rvQuizzes.visibility = android.view.View.GONE
                } else {
                    binding.rvQuizzes.visibility = android.view.View.VISIBLE
                    binding.tvEmptyState.visibility = android.view.View.GONE
                }

                binding.progressBar.visibility = android.view.View.GONE

            } catch (e: Exception) {
                Log.e("QuizHistory", "Error loading quizzes: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@QuizHistoryActivity,
                        "Error loading quizzes: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun onQuizItemClick(quiz: QuizEntity) {
        Log.d("QuizHistory", "Quiz clicked: ${quiz.title} (ID: ${quiz.quizId})")

        // Navigate to QuizTakingActivity with the selected quiz
        val intent = Intent(this, QuizTakingActivity::class.java).apply {
            putExtra("quizId", quiz.quizId)
            putExtra("USERNAME", username)
            putExtra("totalQuestions", quiz.totalQuestions)
        }
        startActivity(intent)
    }
}