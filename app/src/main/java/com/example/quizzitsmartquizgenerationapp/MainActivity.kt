package com.example.quizzit
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find button
        val createQuizBtn = findViewById<Button>(R.id.btnCreateQuiz)

        // On click → go to LoginCredentials
        createQuizBtn.setOnClickListener {
            val intent = Intent(this, LoginCredentials::class.java)
            startActivity(intent)
        }
    }
}
