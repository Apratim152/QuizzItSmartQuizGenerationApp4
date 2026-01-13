package com.example.quizzit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizzit.firebase.FirebaseLeaderboardRepository
import com.example.quizzit.ui.FirebaseLeaderboardAdapter
import com.example.quizzit.databinding.ActivityLeaderboardBinding
import com.example.quizzit.utils.PreferenceManager
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var adapter: FirebaseLeaderboardAdapter
    private val fbLeaderboardRepo = FirebaseLeaderboardRepository()
    private var currentUsername: String = "User"
    private var sortType: SortType = SortType.XP

    enum class SortType {
        XP, AVERAGE_SCORE, QUIZZES_COMPLETED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceManager.init(this)
        currentUsername = intent.getStringExtra("USERNAME") ?: "User"

        setupRecyclerView()
        setupSortButtons()
        loadGlobalLeaderboard()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = FirebaseLeaderboardAdapter(currentUsername)
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
        }
    }

    private fun setupSortButtons() {
        binding.sortByXpButton.setOnClickListener {
            sortType = SortType.XP
            updateSortButtonStates()
            loadGlobalLeaderboard()
        }

        binding.sortByScoreButton.setOnClickListener {
            sortType = SortType.AVERAGE_SCORE
            updateSortButtonStates()
            loadByAverageScore()
        }

        binding.sortByQuizzesButton.setOnClickListener {
            sortType = SortType.QUIZZES_COMPLETED
            updateSortButtonStates()
            loadGlobalLeaderboard()
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

    private fun loadGlobalLeaderboard() {
        binding.progressBar.visibility = View.VISIBLE
        binding.leaderboardRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            fbLeaderboardRepo.getGlobalLeaderboard(100)
                .onSuccess { entries ->
                    binding.progressBar.visibility = View.GONE
                    binding.leaderboardRecyclerView.visibility = View.VISIBLE

                    if (entries.isEmpty()) {
                        binding.emptyStateText?.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateText?.visibility = View.GONE
                        adapter.submitList(entries)
                    }
                    Log.d("Firebase", "✅ Loaded ${entries.size} users")
                }
                .onFailure { error ->
                    Log.e("Firebase", "Error: ${error.message}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@LeaderboardActivity,
                        "Error loading leaderboard: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun loadByAverageScore() {
        binding.progressBar.visibility = View.VISIBLE
        binding.leaderboardRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            fbLeaderboardRepo.getLeaderboardByAverageScore(100)
                .onSuccess { entries ->
                    binding.progressBar.visibility = View.GONE
                    binding.leaderboardRecyclerView.visibility = View.VISIBLE
                    adapter.submitList(entries)
                }
                .onFailure { error ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@LeaderboardActivity,
                        "Error loading leaderboard",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}