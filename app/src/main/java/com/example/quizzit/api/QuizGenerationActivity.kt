package com.example.quizzit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.QuestionEntity
import com.example.quizzit.data.entity.QuizEntity
import com.example.quizzit.databinding.ActivityQuizGenerationBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.jsoup.Jsoup
import java.io.InputStream
import kotlin.math.pow

class QuizGenerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizGenerationBinding
    private lateinit var database: QuizDatabase

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""
    private var username: String = "User"

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY1
        )
    }

    private val BATCH_SIZE = 10
    private val MAX_RETRIES = 3
    private val BATCH_DELAY = 2000L
    private val QUOTA_WAIT = 60000L

    // ---------- File Picker ----------
    private val filePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedFileUri = uri
                selectedFileName = getFileNameFromUri(uri)
                binding.tvSelectedFile.text = "📄 $selectedFileName"
                binding.tvSelectedFile.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Required for Android PDFBox
        PDFBoxResourceLoader.init(applicationContext)

        binding = ActivityQuizGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = QuizDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: "User"

        setupUI()
    }

    // ---------- UI ----------
    private fun setupUI() {

        binding.btnChooseFile.setOnClickListener {
            filePicker.launch("*/*")
        }

        binding.btnClearFile.setOnClickListener {
            selectedFileUri = null
            selectedFileName = ""
            binding.tvSelectedFile.visibility = View.GONE
            toast("File cleared")
        }

        binding.btnGenerateQuiz.setOnClickListener {

            val url = binding.etTopic.text.toString().trim()
            val difficulty = binding.spinnerDifficulty.selectedItem.toString()
            val questionCount =
                binding.etQuestionCount.text.toString().toIntOrNull() ?: 5

            if (url.isEmpty() && selectedFileUri == null) {
                toast("Enter a URL or select a file")
                return@setOnClickListener
            }

            if (url.isNotEmpty() && !isValidUrl(url)) {
                toast("Invalid URL")
                return@setOnClickListener
            }

            if (questionCount !in 1..100) {
                toast("Questions must be between 1 and 100")
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnGenerateQuiz.isEnabled = false

            if (selectedFileUri != null) {
                generateFromFile(selectedFileUri!!, difficulty, questionCount)
            } else {
                generateFromUrl(url, difficulty, questionCount)
            }
        }
    }

    // ---------- FILE ----------
    private fun generateFromFile(uri: Uri, difficulty: String, count: Int) {
        lifecycleScope.launch {
            try {
                val content = extractContentFromFile(uri)
                generateQuestions(content, difficulty, count, selectedFileName)
            } catch (e: Exception) {
                showError(e.message)
            }
        }
    }

    private suspend fun extractContentFromFile(uri: Uri): String =
        withContext(Dispatchers.IO) {

            val inputStream =
                contentResolver.openInputStream(uri)
                    ?: throw Exception("Unable to open file")

            val text = when {
                selectedFileName.endsWith(".pdf", true) ->
                    extractPdfText(inputStream)

                else ->
                    inputStream.bufferedReader().use { it.readText() }
            }

            if (text.length > 32000) text.substring(0, 32000) else text
        }

    private fun extractPdfText(inputStream: InputStream): String {
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()
        return text
    }

    // ---------- URL ----------
    private fun generateFromUrl(url: String, difficulty: String, count: Int) {
        lifecycleScope.launch {
            try {
                val content = extractContentFromUrl(url)
                generateQuestions(content, difficulty, count, url)
            } catch (e: Exception) {
                showError(e.message)
            }
        }
    }

    private suspend fun extractContentFromUrl(url: String): String =
        withContext(Dispatchers.IO) {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get()

            val text = doc.select("article, main, p").text()
            if (text.length > 32000) text.substring(0, 32000) else text
        }

    // ---------- GEMINI ----------
    private suspend fun generateQuestions(
        content: String,
        difficulty: String,
        count: Int,
        source: String
    ) {
        try {
            val questions = mutableListOf<QuestionEntity>()
            val batches = (count + BATCH_SIZE - 1) / BATCH_SIZE

            repeat(batches) {

                var attempt = 0
                var success = false

                while (!success && attempt < MAX_RETRIES) {
                    try {
                        val prompt = buildPrompt(
                            content,
                            difficulty,
                            minOf(BATCH_SIZE, count - questions.size)
                        )

                        val response = generativeModel.generateContent(prompt)
                        questions.addAll(parseQuestions(response.text ?: ""))
                        success = true

                    } catch (e: Exception) {
                        attempt++
                        val delayTime =
                            if (e.message?.contains("quota", true) == true)
                                QUOTA_WAIT
                            else
                                (2.0.pow(attempt) * 1000).toLong()

                        delay(delayTime)
                    }
                }

                delay(BATCH_DELAY)
            }

            val quizId = saveQuiz(source, difficulty, questions)
            navigateToQuiz(quizId, questions.size)

        } catch (e: Exception) {
            showError(e.message)
        }
    }

    private fun buildPrompt(content: String, difficulty: String, count: Int) = """
        Generate exactly $count multiple-choice questions of $difficulty difficulty.

        CONTENT:
        $content

        Respond ONLY with valid JSON:
        [
          {
            "question": "Question text",
            "options": { "A":"...", "B":"...", "C":"...", "D":"..." },
            "correctAnswer": "A"
          }
        ]
    """.trimIndent()

    private fun parseQuestions(text: String): List<QuestionEntity> {
        val clean = text.replace("```json", "").replace("```", "")
        val jsonArray =
            JSONArray(clean.substring(clean.indexOf('['), clean.lastIndexOf(']') + 1))

        return List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            val options = obj.getJSONObject("options")
            val correctKey = obj.getString("correctAnswer")

            QuestionEntity(
                quizOwnerId = 0,
                questionText = obj.getString("question"),
                optionA = options.getString("A"),
                optionB = options.getString("B"),
                optionC = options.getString("C"),
                optionD = options.getString("D"),
                correctOption = options.getString(correctKey)
            )
        }
    }

    // ---------- DATABASE ----------
    private suspend fun saveQuiz(
        source: String,
        difficulty: String,
        questions: List<QuestionEntity>
    ): Int = withContext(Dispatchers.IO) {

        val quiz = QuizEntity(
            title = "Generated Quiz",
            subject = source,
            difficulty = difficulty,
            quizType = "Generated",
            format = "MCQ",
            totalQuestions = questions.size,
            createdAt = System.currentTimeMillis()
        )

        val quizId = database.quizDao().insertQuiz(quiz).toInt()

        database.questionDao().insertQuestions(
            questions.map { it.copy(quizOwnerId = quizId) }
        )

        quizId
    }

    // ---------- NAV ----------
    private fun navigateToQuiz(quizId: Int, total: Int) {
        startActivity(
            Intent(this, QuizTakingActivity::class.java).apply {
                putExtra("quizId", quizId)
                putExtra("USERNAME", username)
                putExtra("totalQuestions", total)
            }
        )
        finish()
    }

    // ---------- HELPERS ----------
    private fun isValidUrl(url: String) =
        url.startsWith("http://") || url.startsWith("https://")

    private fun getFileNameFromUri(uri: Uri): String {

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }

        return uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringBefore('?')
            ?: "Unknown File"
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun showError(msg: String?) {
        toast(msg ?: "Something went wrong")
        binding.progressBar.visibility = View.GONE
        binding.btnGenerateQuiz.isEnabled = true
    }
}
