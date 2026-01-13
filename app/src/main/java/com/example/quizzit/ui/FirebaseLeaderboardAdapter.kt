package com.example.quizzit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzit.R
import com.example.quizzit.firebase.LeaderboardEntry
import com.example.quizzit.databinding.ItemLeaderboardBinding

class FirebaseLeaderboardAdapter(
    private val currentUsername: String
) : ListAdapter<LeaderboardEntry, FirebaseLeaderboardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            binding.apply {
                rankTextView.text = when (entry.rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#${entry.rank}"
                }

                usernameTextView.text = entry.username
                xpTextView.text = "${entry.totalXP} XP"
                quizzesCompletedTextView.text = "${entry.quizzesCompleted} quizzes"
                averageScoreTextView.text = "Avg: ${String.format("%.1f", entry.averageScore)}%"

                if (entry.username == currentUsername) {
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.highlight_current_user)
                    )
                    usernameTextView.setTextColor(
                        ContextCompat.getColor(root.context, R.color.primary_color)
                    )
                } else {
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.white)
                    )
                    usernameTextView.setTextColor(
                        ContextCompat.getColor(root.context, android.R.color.black)
                    )
                }

                if (entry.rank <= 3) {
                    rankTextView.textSize = 24f
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(
            oldItem: LeaderboardEntry,
            newItem: LeaderboardEntry
        ) = oldItem.userId == newItem.userId

        override fun areContentsTheSame(
            oldItem: LeaderboardEntry,
            newItem: LeaderboardEntry
        ) = oldItem == newItem
    }
}