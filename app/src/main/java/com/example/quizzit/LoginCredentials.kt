package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzit.databinding.ActivityLoginCredentialsBinding

class LoginCredentials : AppCompatActivity() {

    private lateinit var binding: ActivityLoginCredentialsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginCredentialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    // ✅ Successful login → go to MainActivity
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("USERNAME", username)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
