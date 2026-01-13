package com.example.quizzit.firebase

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository {
    private val database = Firebase.database
    private val usersRef = database.reference.child("users")

    suspend fun createOrUpdateUser(
        userId: String,
        username: String,
        email: String
    ): Result<Unit> = runCatching {
        val user = FirebaseUserProfile(
            userId = userId,
            username = username,
            email = email,
            updatedAt = System.currentTimeMillis()
        )
        usersRef.child(userId).setValue(user).await()
        Log.d("Firebase", "✅ User created/updated: $username")
    }

    suspend fun getUserProfile(userId: String): Result<FirebaseUserProfile> = runCatching {
        val snapshot = usersRef.child(userId).get().await()
        snapshot.getValue(FirebaseUserProfile::class.java)
            ?: throw Exception("User not found")
    }

    suspend fun getUserByUsername(username: String): Result<FirebaseUserProfile?> = runCatching {
        val snapshot = usersRef.get().await()
        var foundUser: FirebaseUserProfile? = null

        for (child in snapshot.children) {
            val user = child.getValue(FirebaseUserProfile::class.java)
            if (user?.username == username) {
                foundUser = user
                break
            }
        }
        foundUser
    }

    suspend fun updateUserStats(
        userId: String,
        xpEarned: Int,
        score: Int,
        quizzesCompleted: Int = 1
    ): Result<Unit> = runCatching {
        val userSnapshot = usersRef.child(userId).get().await()
        val currentUser = userSnapshot.getValue(FirebaseUserProfile::class.java)
            ?: FirebaseUserProfile()

        val newTotalXP = currentUser.totalXP + xpEarned
        val newQuizzesCompleted = currentUser.quizzesCompleted + quizzesCompleted
        val newTotalScore = currentUser.totalScore + score
        val newAverageScore = if (newQuizzesCompleted > 0) {
            newTotalScore.toFloat() / newQuizzesCompleted
        } else {
            0f
        }
        val newHighestScore = maxOf(currentUser.highestScore, score)

        val updatedUser = currentUser.copy(
            totalXP = newTotalXP,
            quizzesCompleted = newQuizzesCompleted,
            averageScore = newAverageScore,
            highestScore = newHighestScore,
            totalScore = newTotalScore,
            updatedAt = System.currentTimeMillis()
        )

        usersRef.child(userId).setValue(updatedUser).await()
        Log.d("Firebase", "✅ User stats updated: XP=$xpEarned, Score=$score")
    }

    suspend fun getAllUsers(): Result<List<FirebaseUserProfile>> = runCatching {
        val snapshot = usersRef.get().await()
        val users = mutableListOf<FirebaseUserProfile>()

        for (child in snapshot.children) {
            val user = child.getValue(FirebaseUserProfile::class.java)
            if (user != null) {
                users.add(user)
            }
        }
        users
    }
}