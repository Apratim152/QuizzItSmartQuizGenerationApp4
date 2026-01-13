package com.example.quizzit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizzit.data.database.QuizDatabase
import com.example.quizzit.data.entity.UserEntity
import com.example.quizzit.databinding.ActivityLoginCredentialsBinding
import com.example.quizzit.firebase.FirebaseUserRepository
import com.example.quizzit.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class LoginCredentials : AppCompatActivity() {

    private lateinit var binding: ActivityLoginCredentialsBinding
    private lateinit var db: QuizDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginCredentialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QuizDatabase.getDatabase(this)
        PreferenceManager.init(this)
        auth = FirebaseAuth.getInstance()

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
                    lifecycleScope.launch {
                        loginOrRegisterUser(username, email, password)
                    }
                }
            }
        }
    }

    private suspend fun loginOrRegisterUser(username: String, email: String, password: String) {
        var userId = 0
        var firebaseUserId = ""

        withContext(Dispatchers.IO) {
            try {
                // 1. Check local database
                var user = db.userDao().getUserByUsername(username)

                if (user == null) {
                    // New user - register locally
                    val newUser = UserEntity(
                        username = username,
                        email = email
                    )
                    userId = db.userDao().insertUser(newUser).toInt()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginCredentials,
                            "Welcome, $username!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Existing user - login
                    userId = user.userId

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginCredentials,
                            "Welcome back, $username!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // 2. Authenticate with Firebase (using await for proper async handling)
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    firebaseUserId = auth.currentUser?.uid ?: ""
                    Log.d("Firebase", "✅ Firebase user created: $firebaseUserId")
                } catch (createException: Exception) {
                    // User might already exist, try to sign in
                    Log.d("Firebase", "Create failed, attempting sign in: ${createException.message}")
                    try {
                        auth.signInWithEmailAndPassword(email, password).await()
                        firebaseUserId = auth.currentUser?.uid ?: ""
                        Log.d("Firebase", "✅ Firebase user signed in: $firebaseUserId")
                    } catch (signInException: Exception) {
                        Log.e("Firebase", "Sign in failed: ${signInException.message}")
                    }
                }

                Log.d("Firebase", "Current Firebase UID after auth: $firebaseUserId")

                // 3. Sync with Firebase Realtime Database
                if (firebaseUserId.isNotEmpty()) {
                    val fbUserRepo = FirebaseUserRepository()
                    fbUserRepo.createOrUpdateUser(firebaseUserId, username, email)
                        .onSuccess {
                            Log.d("Firebase", "✅ User synced to Firebase: $firebaseUserId")
                        }
                        .onFailure { error ->
                            Log.e("Firebase", "Error syncing to Firebase: ${error.message}")
                            error.printStackTrace()
                        }
                } else {
                    Log.e("Firebase", "❌ Firebase UID is empty - user not authenticated!")
                }

            } catch (e: Exception) {
                Log.e("LoginCredentials", "Error: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginCredentials, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
        }

        // 4. Save to SharedPreferences with Firebase UID
        PreferenceManager.saveLoginInfo(
            username = username,
            userId = userId,
            email = email,
            firebaseUid = firebaseUserId
        )

        Log.d("LoginCredentials", "✅ Saved: username=$username, firebaseUid=$firebaseUserId")

        // 5. Navigate to MainActivity
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