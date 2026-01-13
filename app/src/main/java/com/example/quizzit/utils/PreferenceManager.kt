package com.example.quizzit.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private lateinit var sharedPreferences: SharedPreferences
    private const val PREF_NAME = "QuizzItPrefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_FIREBASE_UID = "firebase_uid"

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save all login info
    fun saveLoginInfo(username: String, userId: Int, email: String, firebaseUid: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            putInt(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putString(KEY_FIREBASE_UID, firebaseUid)
            apply()
        }
    }

    // Username
    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    // User ID
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, 0)
    }

    // Email
    fun getEmail(): String {
        return sharedPreferences.getString(KEY_EMAIL, "") ?: ""
    }

    // Firebase UID
    fun saveFirebaseUid(uid: String) {
        sharedPreferences.edit().putString(KEY_FIREBASE_UID, uid).apply()
    }

    fun getFirebaseUid(): String {
        return sharedPreferences.getString(KEY_FIREBASE_UID, "") ?: ""
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return getUsername().isNotEmpty() && getFirebaseUid().isNotEmpty()
    }

    // Logout
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}