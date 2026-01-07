package com.example.quizzit.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "QuizzItPrefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_EMAIL = "email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save login info
    fun saveLoginInfo(username: String, userId: Int, email: String = "") {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putInt(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get username
    fun getUsername(): String {
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }

    // Get user ID
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, 0)
    }

    // Get email
    fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    // Logout - clear all data
    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    // Get all user info
    fun getUserInfo(): UserInfo? {
        return if (isLoggedIn()) {
            UserInfo(
                username = getUsername(),
                userId = getUserId(),
                email = getEmail()
            )
        } else {
            null
        }
    }
}

data class UserInfo(
    val username: String,
    val userId: Int,
    val email: String
)