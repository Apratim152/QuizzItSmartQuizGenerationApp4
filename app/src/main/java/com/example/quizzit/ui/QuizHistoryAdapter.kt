package com.example.quizzit.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzit.R
import com.example.quizzit.data.entity.QuizEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizHistoryAdapter(
    private val quizzes: MutableList<QuizEntity>,
    private val onItemClick: (QuizEntity) -> Unit
) : RecyclerView.Adapter<QuizHistoryAdapter.QuizViewHolder>() {

    class QuizViewHolder(itemView: View, private val onItemClick: (QuizEntity) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val tvQuizTitle: TextView = itemView.findViewById(R.id.tvQuizTitle)
        val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        val tvQuestionCount: TextView = itemView.findViewById(R.id.tvQuestionCount)
        val tvDateCreated: TextView = itemView.findViewById(R.id.tvDateCreated)
        val tvQuizType: TextView = itemView.findViewById(R.id.tvQuizType)

        fun bind(quiz: QuizEntity) {
            tvQuizTitle.text = quiz.title
            tvDifficulty.text = quiz.difficulty.uppercase()
            tvQuestionCount.text = "${quiz.totalQuestions} Questions"
            tvDateCreated.text = formatDate(quiz.createdAt)
            tvQuizType.text = quiz.quizType

            // Set difficulty badge background color
            tvDifficulty.setBackgroundResource(
                when (quiz.difficulty.lowercase()) {
                    "easy" -> R.drawable.badge_easy
                    "medium" -> R.drawable.badge_medium
                    "hard" -> R.drawable.badge_hard
                    else -> R.drawable.badge_medium
                }
            )

            // Click listener
            itemView.setOnClickListener {
                onItemClick(quiz)
            }
        }

        private fun formatDate(timestamp: Long): String {
            return try {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_history, parent, false)
        return QuizViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(quizzes[position])
    }

    override fun getItemCount(): Int = quizzes.size
}