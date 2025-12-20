package com.example.quizzit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.data.entity.QuizEntity
import com.example.quizzit.ui.GeneratedQuestionsAdapter
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Create Quiz Activity - Simplified without QuizHelper/QuizService/QuizAPI
 * All logic is contained in this single file
 */
class CreateQuizActivity : AppCompatActivity() {

    private lateinit var etQuizTitle: EditText
    private lateinit var etTopicOrPDF: EditText
    private lateinit var etTimeDuration: EditText
    private lateinit var spinnerQuizType: Spinner
    private lateinit var etNumberOfQuestions: EditText
    private lateinit var btnGenerateQuestions: Button
    private lateinit var btnSaveQuiz: Button
    private lateinit var rvGeneratedQuestions: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val generatedQuestions = mutableListOf<QuestionEntity>()
    private lateinit var adapter: GeneratedQuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        initializeViews()
        setupSpinner()
        setupRecyclerView()
        setupButtons()
    }

    private fun initializeViews() {
        etQuizTitle = findViewById(R.id.etQuizTitle)
        etTopicOrPDF = findViewById(R.id.etTopicOrPDF)
        etTimeDuration = findViewById(R.id.etTimeDuration)
        spinnerQuizType = findViewById(R.id.spinnerQuizType)
        etNumberOfQuestions = findViewById(R.id.etNumberOfQuestions)
        btnGenerateQuestions = findViewById(R.id.btnGenerateQuestions)
        btnSaveQuiz = findViewById(R.id.btnSaveQuiz)
        rvGeneratedQuestions = findViewById(R.id.rvGeneratedQuestions)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinner() {
        val quizTypes = arrayOf("Easy", "Medium", "Hard", "Mixed")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quizTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuizType.adapter = spinnerAdapter
        spinnerQuizType.setSelection(1) // Default to Medium
    }

    private fun setupRecyclerView() {
        adapter = GeneratedQuestionsAdapter(generatedQuestions)
        rvGeneratedQuestions.layoutManager = LinearLayoutManager(this)
        rvGeneratedQuestions.adapter = adapter
    }

    private fun setupButtons() {
        btnGenerateQuestions.setOnClickListener {
            if (validateInput()) {
                val topic = etTopicOrPDF.text.toString().trim()
                val difficulty = spinnerQuizType.selectedItem.toString()
                val count = etNumberOfQuestions.text.toString().toIntOrNull() ?: 5

                generateQuestions(topic, difficulty, count)
            }
        }

        btnSaveQuiz.setOnClickListener {
            saveQuizToDatabase()
        }
    }

    private fun validateInput(): Boolean {
        val topic = etTopicOrPDF.text.toString().trim()
        val countText = etNumberOfQuestions.text.toString().trim()

        return when {
            topic.isEmpty() -> {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show()
                etTopicOrPDF.error = "Topic is required"
                false
            }
            countText.isEmpty() -> {
                Toast.makeText(this, "Please enter number of questions", Toast.LENGTH_SHORT).show()
                etNumberOfQuestions.error = "Required"
                false
            }
            countText.toIntOrNull() == null || countText.toInt() !in 1..20 -> {
                Toast.makeText(this, "Question count must be between 1-20", Toast.LENGTH_SHORT).show()
                etNumberOfQuestions.error = "Must be 1-20"
                false
            }
            else -> true
        }
    }

    /**
     * Generate questions using Gemini AI
     */
    private fun generateQuestions(topic: String, difficulty: String, count: Int) {
        lifecycleScope.launch {
            try {
                // Clear previous questions
                generatedQuestions.clear()
                adapter.notifyDataSetChanged()

                // Show loading state
                showLoading(true)

                // Create prompt for Gemini
                val prompt = createPrompt(topic, difficulty, count)
                Log.d("CreateQuiz", "Prompt: $prompt")

                // Call Gemini API directly
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.0-flash-exp",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: ""
                Log.d("CreateQuiz", "Response: $responseText")

                // Parse JSON response
                val questions = parseQuestions(responseText)

                if (questions.isEmpty()) {
                    Toast.makeText(
                        this@CreateQuizActivity,
                        "No questions generated. Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    generatedQuestions.addAll(questions)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(
                        this@CreateQuizActivity,
                        "Generated ${questions.size} questions!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("CreateQuiz", "Error generating questions", e)
                Toast.makeText(
                    this@CreateQuizActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * Create prompt for Gemini AI
     */
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
            - Make questions clear, concise, and educational
            - Ensure all options are plausible but only one is correct
            - Return ONLY the JSON array, no extra text before or after
        """.trimIndent()
    }

    /**
     * Parse JSON response from Gemini into QuestionEntity list
     */
    private fun parseQuestions(responseText: String): List<QuestionEntity> {
        val questions = mutableListOf<QuestionEntity>()

        try {
            // Clean the response (remove markdown code blocks if present)
            val cleanedJson = extractValidJson(responseText)
            Log.d("CreateQuiz", "Cleaned JSON: $cleanedJson")

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

                // Map correct answer key to actual text
                val correctAnswerText = when (correctAnswerKey.uppercase()) {
                    "A" -> optionA
                    "B" -> optionB
                    "C" -> optionC
                    "D" -> optionD
                    else -> optionA // fallback
                }

                val question = QuestionEntity(
                    quizOwnerId = 0, // Will be set when saving
                    questionText = questionText,
                    optionA = optionA,
                    optionB = optionB,
                    optionC = optionC,
                    optionD = optionD,
                    correctOption = correctAnswerText
                )

                questions.add(question)
            }

            Log.d("CreateQuiz", "Successfully parsed ${questions.size} questions")

        } catch (e: Exception) {
            Log.e("CreateQuiz", "Error parsing JSON: ${e.message}", e)
            throw Exception("Failed to parse quiz questions: ${e.message}")
        }

        return questions
    }

    /**
     * Extract valid JSON from response (handles markdown code blocks)
     */
    private fun extractValidJson(text: String): String {
        // Remove markdown code blocks
        var cleaned = text.trim()

        // Remove ```json and ``` markers
        cleaned = cleaned.replace("```json", "").replace("```", "").trim()

        // Find JSON array boundaries
        val startIndex = cleaned.indexOf('[')
        val endIndex = cleaned.lastIndexOf(']')

        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned.substring(startIndex, endIndex + 1)
        } else {
            throw Exception("Invalid JSON format in response")
        }
    }

    /**
     * Save quiz to database
     */
    private fun saveQuizToDatabase() {
        val quizTitle = etQuizTitle.text.toString().trim()
        val topic = etTopicOrPDF.text.toString().trim()
        val difficulty = spinnerQuizType.selectedItem.toString()
        val timeDuration = etTimeDuration.text.toString().toIntOrNull() ?: 10

        // Validate before saving
        when {
            quizTitle.isEmpty() -> {
                Toast.makeText(this, "Please enter a quiz title", Toast.LENGTH_SHORT).show()
                etQuizTitle.error = "Title is required"
                return
            }
            generatedQuestions.isEmpty() -> {
                Toast.makeText(this, "Please generate questions first", Toast.LENGTH_SHORT).show()
                return
            }
        }

        lifecycleScope.launch {
            try {
                val db = QuizDatabase.getDatabase(this@CreateQuizActivity)

                val quiz = QuizEntity(
                    title = quizTitle,
                    subject = topic,
                    difficulty = difficulty,
                    totalQuestions = generatedQuestions.size,
                    quizType = "AI Generated",
                    format = "Multiple Choice"
                )

                withContext(Dispatchers.IO) {
                    // Insert quiz and get its ID
                    val quizId = db.quizDao().insertQuiz(quiz).toInt()

                    // Update questions with quiz ID and insert
                    val questionsWithQuizId = generatedQuestions.map {
                        it.copy(quizOwnerId = quizId)
                    }
                    db.questionDao().insertQuestions(questionsWithQuizId)

                    Log.d("CreateQuiz", "Saved quiz with ID: $quizId")
                }

                Toast.makeText(
                    this@CreateQuizActivity,
                    "Quiz saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Clear form
                etQuizTitle.text.clear()
                etTopicOrPDF.text.clear()
                etTimeDuration.text.clear()
                etNumberOfQuestions.text.clear()
                generatedQuestions.clear()
                adapter.notifyDataSetChanged()

                // Optionally: Navigate back or to quiz list
                finish()

            } catch (e: Exception) {
                Log.e("CreateQuiz", "Error saving quiz", e)
                Toast.makeText(
                    this@CreateQuizActivity,
                    "Failed to save quiz: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Show/hide loading state
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnGenerateQuestions.isEnabled = false
            btnGenerateQuestions.text = "Generating..."
        } else {
            progressBar.visibility = View.GONE
            btnGenerateQuestions.isEnabled = true
            btnGenerateQuestions.text = "Generate Questions"
        }
    }
}