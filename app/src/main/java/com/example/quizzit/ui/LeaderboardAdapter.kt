package com.example.quizzit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzit.R
import com.example.quizzit.data.entity.UserEntity
import com.example.quizzit.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(
    private val currentUsername: String
) : ListAdapter<UserEntity, LeaderboardAdapter.LeaderboardViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class LeaderboardViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity, rank: Int) {
            binding.apply {
                // Set rank with medal emoji for top 3
                rankTextView.text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "$rank"
                }

                usernameTextView.text = user.username
                xpTextView.text = "${user.totalXP} XP"
                quizzesCompletedTextView.text = "${user.quizzesCompleted} quizzes"
                averageScoreTextView.text = "Avg: ${String.format("%.1f", user.averageScore)}%"

                // Highlight current user with subtle background
                if (user.username == currentUsername) {
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

                // Special styling for top 3
                if (rank <= 3) {
                    rankTextView.textSize = 24f
                } else {
                    rankTextView.textSize = 18f
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserEntity>() {
        override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem == newItem
        }
    }
}