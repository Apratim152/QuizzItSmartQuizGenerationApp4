package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.UserEntity
import com.example.quizzit.databinding.ActivityLoginCredentialsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginCredentials : AppCompatActivity() {

    private lateinit var binding: ActivityLoginCredentialsBinding
    private lateinit var db: QuizDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginCredentialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)

        binding.loginButton.setOnClickListener {

            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            when {
                username.isEmpty() -> {
                    binding.usernameEditText.error = "Username is required"
                    Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show()
                }

                email.isEmpty() -> {
                    binding.emailEditText.error = "Email is required"
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.emailEditText.error = "Invalid email format"
                    Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show()
                }

                password.isEmpty() -> {
                    binding.passwordEditText.error = "Password is required"
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                }

                password.length < 6 -> {
                    binding.passwordEditText.error = "Password must be at least 6 characters"
                    Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    // Check if user exists, if not create new user
                    lifecycleScope.launch {
                        loginOrRegisterUser(username, email)
                    }
                }
            }
        }
    }

    private suspend fun loginOrRegisterUser(username: String, email: String) {
        withContext(Dispatchers.IO) {
            var user = db.userDao().getUserByUsername(username)

            if (user == null) {
                // New user - register
                val newUser = UserEntity(
                    username = username,
                    email = email
                )
                db.userDao().insertUser(newUser)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginCredentials,
                        "Welcome, $username!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Existing user - login
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginCredentials,
                        "Welcome back, $username!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Navigate to MainActivity
        withContext(Dispatchers.Main) {
            val intent = Intent(this@LoginCredentials, MainActivity::class.java).apply {
                putExtra("USERNAME", username)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}