package com.example.nexthire.data

import android.content.Context

class ProfileStorage(context: Context, email: String) {

    private val prefs = context.getSharedPreferences("profile_$email", Context.MODE_PRIVATE)

    fun saveProfile(map: Map<String, String>) {
        val editor = prefs.edit()
        map.forEach { editor.putString(it.key, it.value) }
        editor.apply()
    }

    fun loadProfile(): Map<String, String> {
        return mapOf(
            "name" to prefs.getString("name", "")!!,
            "title" to prefs.getString("title", "")!!,
            "about" to prefs.getString("about", "")!!,
            "email" to prefs.getString("email", "")!!,
            "phone" to prefs.getString("phone", "")!!,
            "location" to prefs.getString("location", "")!!,
            "skills" to prefs.getString("skills", "")!!,
            "experience" to prefs.getString("experience", "")!!,
            "jobType" to prefs.getString("jobType", "")!!,
            "salary" to prefs.getString("salary", "")!!,
            "profileImage" to prefs.getString("profileImage", "")!!
        )
    }

    fun saveExtra(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getExtra(key: String): String? {
        return prefs.getString(key, null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
