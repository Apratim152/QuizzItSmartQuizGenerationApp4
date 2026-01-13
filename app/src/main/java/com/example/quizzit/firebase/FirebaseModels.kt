package com.example.quizzit.firebase

data class FirebaseUserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val totalXP: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageScore: Float = 0f,
    val highestScore: Int = 0,
    val totalScore: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val totalXP: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageScore: Float = 0f,
    val rank: Int = 0
)