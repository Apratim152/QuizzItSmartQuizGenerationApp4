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
import org.jsoup.Jsoup
import java.io.IOException

class QuizGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizGenerationBinding
    private lateinit var db: QuizDatabase
    private var username: String = "User"

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
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
            val url = binding.etTopic.text.toString().trim()
            val difficulty = binding.spinnerDifficulty.selectedItem.toString()
            val questionCount = binding.etQuestionCount.text.toString().toIntOrNull() ?: 5

            Log.d("QuizGeneration", "Generate button clicked - URL: $url, Difficulty: $difficulty, Count: $questionCount")

            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidUrl(url)) {
                Toast.makeText(this, "Please enter a valid URL (must start with http:// or https://)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (questionCount < 1 || questionCount > 20) {
                Toast.makeText(this, "Question count should be between 1-20", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateQuizFromUrl(url, difficulty, questionCount)
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun generateQuizFromUrl(url: String, difficulty: String, count: Int) {
        binding.btnGenerateQuiz.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        Toast.makeText(this, "Fetching content from URL...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                Log.d("QuizGeneration", "Starting URL content extraction...")

                // Step 1: Extract content from URL
                val pageContent = withContext(Dispatchers.IO) {
                    extractContentFromUrl(url)
                }

                if (pageContent.isEmpty()) {
                    throw Exception("Failed to extract content from URL")
                }

                Log.d("QuizGeneration", "Extracted ${pageContent.length} characters from URL")
                Toast.makeText(this@QuizGenerationActivity, "Content extracted! Generating quiz...", Toast.LENGTH_SHORT).show()

                // Step 2: Create prompt with extracted content
                val prompt = createPromptWithContent(pageContent, difficulty, count)
                Log.d("QuizPrompt", "Prompt created with content length: ${prompt.length}")

                // Step 3: Call Gemini API
                Log.d("QuizGeneration", "Calling Gemini API...")
                val response = generativeModel.generateContent(prompt)

                val responseText = response.text ?: ""
                Log.d("QuizResponse", "Response received: ${responseText.take(200)}...")

                // Step 4: Parse JSON response
                val questions = parseQuestions(responseText)
                Log.d("QuizGeneration", "Parsed ${questions.size} questions")

                if (questions.isEmpty()) {
                    throw Exception("Failed to generate questions")
                }

                // Step 5: Save to database
                val quizId = withContext(Dispatchers.IO) {
                    saveQuizToDatabase(url, difficulty, questions)
                }
                Log.d("QuizGeneration", "Quiz saved with ID: $quizId")

                // Step 6: Navigate to quiz
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

    /**
     * Extract text content from URL using Jsoup
     */
    private suspend fun extractContentFromUrl(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("URLExtraction", "Connecting to: $url")

                // Fetch and parse the webpage
                val document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000) // 15 second timeout
                    .get()

                // Extract title
                val title = document.title()

                // Extract main content (prioritize article/main content areas)
                val content = StringBuilder()

                // Try to find main content areas first
                val mainContent = document.select("article, main, .content, .post-content, .article-content")
                    .text()

                if (mainContent.isNotEmpty()) {
                    content.append(mainContent)
                } else {
                    // Fallback: extract all paragraph text
                    val paragraphs = document.select("p")
                        .map { it.text() }
                        .filter { it.length > 50 } // Filter out short snippets
                        .joinToString(" ")
                    content.append(paragraphs)
                }

                // Also get heading text for context
                val headings = document.select("h1, h2, h3")
                    .map { it.text() }
                    .joinToString(". ")

                val fullContent = "$title. $headings. ${content.toString()}"

                // Limit content to avoid token limits (approximately 8000 words)
                val limitedContent = if (fullContent.length > 32000) {
                    fullContent.substring(0, 32000) + "..."
                } else {
                    fullContent
                }

                Log.d("URLExtraction", "Successfully extracted ${limitedContent.length} characters")
                limitedContent

            } catch (e: IOException) {
                Log.e("URLExtraction", "IO Error: ${e.message}", e)
                throw Exception("Failed to fetch URL: ${e.message}")
            } catch (e: Exception) {
                Log.e("URLExtraction", "Error: ${e.message}", e)
                throw Exception("Failed to extract content: ${e.message}")
            }
        }
    }

    /**
     * Create prompt with extracted webpage content
     */
    private fun createPromptWithContent(content: String, difficulty: String, count: Int): String {
        return """
            Based on the following webpage content, generate exactly $count multiple-choice quiz questions at $difficulty difficulty level.
            
            WEBPAGE CONTENT:
            $content
            
            Generate questions that test understanding of the key concepts, facts, and information from this content.
            
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
            - Questions should be directly based on the content provided
            - Make questions engaging and test real understanding
            - Return ONLY the JSON array, no extra text before or after
        """.trimIndent()
    }

    private fun parseQuestions(responseText: String): List<QuestionEntity> {
        val questions = mutableListOf<QuestionEntity>()

        try {
            val cleanedJson = extractValidJson(responseText)
            Log.d("QuizParsing", "Cleaned JSON: ${cleanedJson.take(200)}...")

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
                Log.d("QuizParsing", "Added question ${i + 1}: ${questionText.take(50)}...")
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
        url: String,
        difficulty: String,
        questions: List<QuestionEntity>
    ): Int {
        return try {
            Log.d("QuizDatabase", "Saving quiz to database...")

            // Extract domain name for title
            val title = try {
                val domain = url.substringAfter("://").substringBefore("/")
                "Quiz from $domain"
            } catch (e: Exception) {
                "Quiz from URL"
            }

            val quiz = QuizEntity(
                title = title,
                subject = url,
                difficulty = difficulty,
                quizType = "URL Generated",
                format = "Multiple Choice",
                totalQuestions = questions.size,
                createdAt = System.currentTimeMillis()
            )

            val quizId = db.quizDao().insertQuiz(quiz).toInt()
            Log.d("QuizDatabase", "Quiz inserted with ID: $quizId")

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