package com.example.quizzit.firebase

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseLeaderboardRepository {
    private val database = Firebase.database
    private val usersRef = database.reference.child("users")

    suspend fun getGlobalLeaderboard(limit: Long = 100): Result<List<LeaderboardEntry>> = runCatching {
        val snapshot = usersRef.get().await()
        val users = mutableListOf<FirebaseUserProfile>()

        for (child in snapshot.children) {
            val user = child.getValue(FirebaseUserProfile::class.java)
            if (user != null) {
                users.add(user)
            }
        }

        Log.d("Firebase", "Loaded ${users.size} users from leaderboard")

        // Sort by totalXP descending
        users.sortByDescending { it.totalXP }

        // Take only the limit
        val limitedUsers = users.take(limit.toInt())

        limitedUsers.mapIndexed { index, user ->
            LeaderboardEntry(
                userId = user.userId,
                username = user.username,
                totalXP = user.totalXP,
                quizzesCompleted = user.quizzesCompleted,
                averageScore = user.averageScore,
                rank = index + 1
            )
        }
    }

    suspend fun getLeaderboardByAverageScore(limit: Long = 100): Result<List<LeaderboardEntry>> = runCatching {
        val snapshot = usersRef.get().await()
        val users = mutableListOf<FirebaseUserProfile>()

        for (child in snapshot.children) {
            val user = child.getValue(FirebaseUserProfile::class.java)
            if (user != null) {
                users.add(user)
            }
        }

        // Sort by averageScore descending
        users.sortByDescending { it.averageScore }

        // Take only the limit
        val limitedUsers = users.take(limit.toInt())

        limitedUsers.mapIndexed { index, user ->
            LeaderboardEntry(
                userId = user.userId,
                username = user.username,
                totalXP = user.totalXP,
                quizzesCompleted = user.quizzesCompleted,
                averageScore = user.averageScore,
                rank = index + 1
            )
        }
    }

    suspend fun getUserRank(userId: String): Result<Int> = runCatching {
        val userSnapshot = usersRef.child(userId).get().await()
        val user = userSnapshot.getValue(FirebaseUserProfile::class.java)
            ?: throw Exception("User not found")

        val allSnapshot = usersRef.get().await()
        var betterCount = 0

        for (child in allSnapshot.children) {
            val otherUser = child.getValue(FirebaseUserProfile::class.java)
            if (otherUser != null && otherUser.totalXP > user.totalXP) {
                betterCount++
            }
        }

        betterCount + 1
    }
}