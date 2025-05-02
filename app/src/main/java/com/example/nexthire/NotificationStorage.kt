package com.example.nexthire

import android.content.Context
import com.example.nexthire.model.NotificationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotificationStorage {

    private const val KEY_MESSAGES = "notification_list"
    private val gson = Gson()

    private fun prefs(context: Context, userEmail: String) =
        context.getSharedPreferences("notifications_$userEmail", Context.MODE_PRIVATE)

    fun append(context: Context, item: NotificationItem, userEmail: String) {
        val list = loadAll(context, userEmail).toMutableList()

        // ✅ Prevent duplicate by jobId
        if (list.none { it.jobId == item.jobId }) {
            list.add(0, item) // Add to top
            val json = gson.toJson(list)
            prefs(context, userEmail).edit().putString(KEY_MESSAGES, json).apply()
        }
    }

    fun loadAll(context: Context, userEmail: String): List<NotificationItem> {
        val json = prefs(context, userEmail).getString(KEY_MESSAGES, null) ?: return emptyList()
        val type = object : TypeToken<List<NotificationItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clear(context: Context, userEmail: String) {
        prefs(context, userEmail).edit().remove(KEY_MESSAGES).apply()
    }
}
