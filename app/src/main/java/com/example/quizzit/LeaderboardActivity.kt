package com.example.quizzit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzit.ui.LeaderboardAdapter
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.UserEntity
import com.example.quizzit.databinding.ActivityLeaderboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var db: QuizDatabase
    private lateinit var adapter: LeaderboardAdapter
    private var currentUsername: String = "User"
    private var sortType: SortType = SortType.XP

    enum class SortType {
        XP, AVERAGE_SCORE, QUIZZES_COMPLETED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)
        currentUsername = intent.getStringExtra("USERNAME") ?: "User"

        setupRecyclerView()
        setupSortButtons()
        loadLeaderboard(sortType)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(currentUsername)
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
        }
    }

    private fun setupSortButtons() {
        binding.sortByXpButton.setOnClickListener {
            sortType = SortType.XP
            updateSortButtonStates()
            loadLeaderboard(sortType)
        }

        binding.sortByScoreButton.setOnClickListener {
            sortType = SortType.AVERAGE_SCORE
            updateSortButtonStates()
            loadLeaderboard(sortType)
        }

        binding.sortByQuizzesButton.setOnClickListener {
            sortType = SortType.QUIZZES_COMPLETED
            updateSortButtonStates()
            loadLeaderboard(sortType)
        }
    }

    private fun updateSortButtonStates() {
        // Reset all buttons
        binding.sortByXpButton.isSelected = false
        binding.sortByScoreButton.isSelected = false
        binding.sortByQuizzesButton.isSelected = false

        // Highlight selected button
        when (sortType) {
            SortType.XP -> binding.sortByXpButton.isSelected = true
            SortType.AVERAGE_SCORE -> binding.sortByScoreButton.isSelected = true
            SortType.QUIZZES_COMPLETED -> binding.sortByQuizzesButton.isSelected = true
        }
    }

    private fun loadLeaderboard(type: SortType) {
        binding.progressBar.visibility = View.VISIBLE
        binding.leaderboardRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) {
                when (type) {
                    SortType.XP -> db.userDao().getAllUsersByXP()
                    SortType.AVERAGE_SCORE -> db.userDao().getAllUsersByAverageScore()
                    SortType.QUIZZES_COMPLETED -> db.userDao().getAllUsersByQuizzesCompleted()
                }
            }

            binding.progressBar.visibility = View.GONE
            binding.leaderboardRecyclerView.visibility = View.VISIBLE

            if (users.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
            } else {
                binding.emptyStateText.visibility = View.GONE
                adapter.submitList(users)
            }
        }
    }
}