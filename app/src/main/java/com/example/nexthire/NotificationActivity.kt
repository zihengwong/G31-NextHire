package com.example.nexthire

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexthire.adapter.NotificationAdapter
import com.example.nexthire.data.JobRepository
import com.example.nexthire.model.NotificationItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.webkit.WebView

class NotificationActivity : AppCompatActivity() {

    private val notifications = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var emptyText: TextView
    private lateinit var clearAllIcon: ImageView
    private lateinit var currentUser: String
    private var isActivityAlive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Initialize WebView for graphics stability
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val process = applicationInfo.processName
            if (!packageName.equals(process, ignoreCase = true)) {
                WebView.setDataDirectorySuffix(process)
            }
        }

        initializeViews()
        setupUserPreferences()
        loadNotifications()
        setupClearButton()
        setupBottomNavigation()
    }

    private fun initializeViews() {
        emptyText = findViewById(R.id.emptyNotificationText)
        clearAllIcon = findViewById(R.id.clearAllIcon)
        val recyclerView = findViewById<RecyclerView>(R.id.notificationRecyclerView)

        adapter = NotificationAdapter(notifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupUserPreferences() {
        val userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUser = userPrefs.getString("email", "guest") ?: "guest"
    }

    private fun loadNotifications() {
        if (!isActivityAlive) return

        // Load existing notifications
        notifications.addAll(NotificationStorage.loadAll(this, currentUser))

        // Handle incoming new matches from intent
        val matches = intent.getStringArrayListExtra("matches")
        if (matches != null) {
            handleNewMatches(matches)
        }

        updateVisibility()
    }

    private fun handleNewMatches(matches: ArrayList<String>) {
        if (!isActivityAlive) return

        val existingIds = notifications.map { it.jobId }.toSet()
        val newItems = JobRepository.jobs
            .filter { job ->
                val msg = "🎯 ${job.title} at ${job.company} — ${job.salary}"
                msg in matches && job.id !in existingIds
            }
            .map { job ->
                NotificationItem(
                    message = "🎯 ${job.title} at ${job.company} — ${job.salary}",
                    time = getCurrentTime(),
                    jobId = job.id,
                    jobTitle = job.title,
                    jobCompany = job.company
                )
            }

        newItems.forEach {
            notifications.add(0, it)
            NotificationStorage.append(this, it, currentUser)
            Log.d("NOTIF_DEBUG", "Added new notification: ${it.message}")
        }
    }

    private fun setupClearButton() {
        clearAllIcon.setOnClickListener {
            if (!isActivityAlive) return@setOnClickListener

            notifications.clear()
            NotificationStorage.clear(this, currentUser)
            adapter.notifyDataSetChanged()
            updateVisibility()
            showToast("Notifications cleared")
        }
    }

    private fun updateVisibility() {
        if (!isActivityAlive) return

        runOnUiThread {
            emptyText.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
            clearAllIcon.visibility = if (notifications.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_notifications
        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java).apply {
                        putExtra("navigateTo", "home")
                    })
                    true
                }
                R.id.nav_saved -> {
                    startActivity(Intent(this, DashboardActivity::class.java).apply {
                        putExtra("navigateTo", "saved")
                    })
                    true
                }
                R.id.nav_applied -> {
                    startActivity(Intent(this, DashboardActivity::class.java).apply {
                        putExtra("navigateTo", "applied")
                    })
                    true
                }
                R.id.nav_notifications -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    private fun showToast(message: String) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityAlive = false
    }
}