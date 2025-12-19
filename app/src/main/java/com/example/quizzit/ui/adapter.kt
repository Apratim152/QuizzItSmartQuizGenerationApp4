package com.example.quizzit.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzit.R
import com.example.quizzit.data.entity.QuestionEntity

class GeneratedQuestionsAdapter(
    private val questions: MutableList<QuestionEntity>
) : RecyclerView.Adapter<GeneratedQuestionsAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestionText: TextView = itemView.findViewById(R.id.tvQuestionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_generated_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.tvQuestionText.text = questions[position].questionText
    }

    override fun getItemCount(): Int = questions.size
}
