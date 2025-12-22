package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.data.entity.QuizEntity
import com.example.quizzit.databinding.ActivityQuizGenerationBinding
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class QuizGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizGenerationBinding
    private lateinit var db: QuizDatabase
    private var username: String = "User"

    // ✅ Step 3: Initialize GenerativeModel (Following Official Tutorial)
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash", // Using flash model for speed
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: "User"

        Log.d("QuizGeneration", "Activity created, username: $username")
        setupUI()
    }

    private fun setupUI() {
        binding.btnGenerateQuiz.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            val difficulty = binding.spinnerDifficulty.selectedItem.toString()
            val questionCount = binding.etQuestionCount.text.toString().toIntOrNull() ?: 5

            Log.d("QuizGeneration", "Generate button clicked - Topic: $topic, Difficulty: $difficulty, Count: $questionCount")

            if (topic.isEmpty()) {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (questionCount < 1 || questionCount > 20) {
                Toast.makeText(this, "Question count should be between 1-20", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateQuiz(topic, difficulty, questionCount)
        }
    }

    private fun generateQuiz(topic: String, difficulty: String, count: Int) {
        binding.btnGenerateQuiz.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        Toast.makeText(this, "Generating quiz...", Toast.LENGTH_SHORT).show()

        // ✅ Step 4: Use lifecycleScope.launch (Following Official Tutorial)
        lifecycleScope.launch {
            try {
                Log.d("QuizGeneration", "Starting quiz generation...")

                // 1. Create prompt
                val prompt = createPrompt(topic, difficulty, count)
                Log.d("QuizPrompt", prompt)

                // ✅ Step 4: Call generateContent (suspend function)
                Log.d("QuizGeneration", "Calling Gemini API...")
                val response = generativeModel.generateContent(prompt)

                // ✅ Step 4: Handle the result using response.text
                val responseText = response.text ?: ""
                Log.d("QuizResponse", "Response received: $responseText")

                // 2. Parse JSON response
                val questions = parseQuestions(responseText)
                Log.d("QuizGeneration", "Parsed ${questions.size} questions")

                if (questions.isEmpty()) {
                    throw Exception("Failed to generate questions")
                }

                // 3. Save to database
                val quizId = withContext(Dispatchers.IO) {
                    saveQuizToDatabase(topic, difficulty, questions)
                }
                Log.d("QuizGeneration", "Quiz saved with ID: $quizId")

                // 4. Navigate to quiz
                Toast.makeText(this@QuizGenerationActivity, "Quiz generated!", Toast.LENGTH_SHORT).show()
                goToQuiz(quizId, questions.size)

            } catch (e: Exception) {
                Log.e("QuizGeneration", "Error: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(
                    this@QuizGenerationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnGenerateQuiz.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun createPrompt(topic: String, difficulty: String, count: Int): String {
        return """
            Generate exactly $count multiple-choice quiz questions about "$topic" at $difficulty difficulty level.
            
            Return ONLY a valid JSON array with this exact structure (no markdown, no extra text):
            [
              {
                "question": "Question text here?",
                "options": {
                  "A": "First option",
                  "B": "Second option",
                  "C": "Third option",
                  "D": "Fourth option"
                },
                "correctAnswer": "A"
              }
            ]
            
            Rules:
            - Each question must have exactly 4 options (A, B, C, D)
            - correctAnswer must be one of: "A", "B", "C", or "D"
            - Questions should be appropriate for $difficulty difficulty
            - Make questions engaging and educational
            - Return ONLY the JSON array, no extra text before or after
        """.trimIndent()
    }

    private fun parseQuestions(responseText: String): List<QuestionEntity> {
        val questions = mutableListOf<QuestionEntity>()

        try {
            val cleanedJson = extractValidJson(responseText)
            Log.d("QuizParsing", "Cleaned JSON: $cleanedJson")

            val jsonArray = JSONArray(cleanedJson)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val questionText = jsonObject.getString("question")
                val options = jsonObject.getJSONObject("options")
                val correctAnswerKey = jsonObject.getString("correctAnswer")

                val optionA = options.getString("A")
                val optionB = options.getString("B")
                val optionC = options.getString("C")
                val optionD = options.getString("D")

                val correctAnswerText = when (correctAnswerKey.uppercase()) {
                    "A" -> optionA
                    "B" -> optionB
                    "C" -> optionC
                    "D" -> optionD
                    else -> optionA
                }

                val question = QuestionEntity(
                    quizOwnerId = 0,
                    questionText = questionText,
                    optionA = optionA,
                    optionB = optionB,
                    optionC = optionC,
                    optionD = optionD,
                    correctOption = correctAnswerText
                )

                questions.add(question)
                Log.d("QuizParsing", "Added question ${i + 1}: $questionText")
            }

            Log.d("QuizParsing", "Successfully parsed ${questions.size} questions")

        } catch (e: Exception) {
            Log.e("QuizParsing", "Error parsing JSON: ${e.message}", e)
            e.printStackTrace()
            throw Exception("Failed to parse quiz questions: ${e.message}")
        }

        return questions
    }

    private fun extractValidJson(text: String): String {
        var cleaned = text.trim()
        cleaned = cleaned.replace("```json", "").replace("```", "").trim()

        val startIndex = cleaned.indexOf('[')
        val endIndex = cleaned.lastIndexOf(']')

        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned.substring(startIndex, endIndex + 1)
        } else {
            throw Exception("Invalid JSON format in response")
        }
    }

    private suspend fun saveQuizToDatabase(
        topic: String,
        difficulty: String,
        questions: List<QuestionEntity>
    ): Int {
        return try {
            Log.d("QuizDatabase", "Saving quiz to database...")

            // Create quiz entity
            val quiz = QuizEntity(
                title = topic,
                subject = topic,
                difficulty = difficulty,
                quizType = "AI Generated",
                format = "Multiple Choice",
                totalQuestions = questions.size,
                createdAt = System.currentTimeMillis()
            )

            // Insert quiz and get its ID
            val quizId = db.quizDao().insertQuiz(quiz).toInt()
            Log.d("QuizDatabase", "Quiz inserted with ID: $quizId")

            // Update questions with quiz ID and insert
            val questionsWithQuizId = questions.map { it.copy(quizOwnerId = quizId) }
            db.questionDao().insertQuestions(questionsWithQuizId)
            Log.d("QuizDatabase", "Inserted ${questionsWithQuizId.size} questions")

            quizId

        } catch (e: Exception) {
            Log.e("QuizDatabase", "Error saving to database: ${e.message}", e)
            e.printStackTrace()
            throw Exception("Failed to save quiz: ${e.message}")
        }
    }

    private fun goToQuiz(quizId: Int, totalQuestions: Int) {
        try {
            Log.d("QuizGeneration", "Navigating to QuizTakingActivity with quizId: $quizId")

            val intent = Intent(this, QuizTakingActivity::class.java).apply {
                putExtra("quizId", quizId)
                putExtra("USERNAME", username)
                putExtra("totalQuestions", totalQuestions)
            }

            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e("QuizGeneration", "Error starting QuizTakingActivity: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "Error opening quiz: ${e.message}", Toast.LENGTH_LONG).show()
            binding.btnGenerateQuiz.isEnabled = true
            binding.progressBar.visibility = android.view.View.GONE
        }
    }
}