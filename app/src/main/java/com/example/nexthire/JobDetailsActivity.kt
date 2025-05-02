package com.example.nexthire

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nexthire.model.Job
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var saveButton: ImageButton
    private lateinit var applyButton: MaterialButton
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var job: Job
    private val gson = Gson()
    private var currentUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        // 🔐 Get current logged-in user's email
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUser = userPrefs.getString("email", null)
        sharedPrefs = getSharedPreferences("profile_${currentUser ?: "guest"}", Context.MODE_PRIVATE)

        // ✅ Get job data from intent
        val title = intent.getStringExtra("title") ?: ""
        val company = intent.getStringExtra("company") ?: ""
        val location = intent.getStringExtra("location") ?: ""
// ✅ Now safe to use all 3
        val jobId = intent.getStringExtra("id") ?: "${title}_${company}_${location}"
        val salary = intent.getStringExtra("salary") ?: ""
        val type = intent.getStringExtra("jobType") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val experience = intent.getStringExtra("experience") ?: ""

// ✅ Construct job
        job = Job(jobId, title, company, location, type, salary, description, experience)

        job.isSaved = isJobSaved()
        job.applied = isJobAlreadyApplied()

        // 👀 Bind UI
        findViewById<TextView>(R.id.job_title).text = title
        findViewById<TextView>(R.id.job_company).text = company
        findViewById<TextView>(R.id.job_location).text = location
        findViewById<TextView>(R.id.job_salary).text = salary
        findViewById<TextView>(R.id.job_type).text = type
        findViewById<TextView>(R.id.job_description).text = description

        // Back nav
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        applyButton = findViewById(R.id.apply_button)
        saveButton = findViewById(R.id.save_button)

        // 🧠 Apply button logic
        if (currentUser == null) {
            applyButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Login Required")
                    .setMessage("Please log in to apply for this job.")
                    .setPositiveButton("Login") { _, _ ->
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            if (job.applied) {
                markAsApplied()
            } else {
                applyButton.setOnClickListener {
                    saveApplicationToLocal()
                    markAsApplied()
                    Toast.makeText(this, "🎉 Application submitted!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 🔖 Save icon
        updateSaveIcon(job.isSaved)

        saveButton.setOnClickListener {
            toggleJobSave()
        }
    }

    private fun getJobKey(): String = "${job.title}_${job.company}_${job.location}"

    private fun getUserPref(name: String): SharedPreferences {
        return getSharedPreferences("${name}_$currentUser", Context.MODE_PRIVATE)
    }

    private fun isJobAlreadyApplied(): Boolean {
        val prefs = getUserPref("applied_jobs")
        val json = prefs.getString("jobs", null)
        val type = object : TypeToken<List<Job>>() {}.type
        val list = gson.fromJson<List<Job>>(json, type) ?: return false
        return list.any { it.title == job.title && it.company == job.company }
    }

    private fun saveApplicationToLocal() {
        val prefs = getUserPref("applied_jobs")
        val json = prefs.getString("jobs", null)
        val type = object : TypeToken<MutableList<Job>>() {}.type
        val list = gson.fromJson<MutableList<Job>>(json, type) ?: mutableListOf()

        if (list.none { it.title == job.title && it.company == job.company }) {
            list.add(job)
            prefs.edit().putString("jobs", gson.toJson(list)).apply()
        }
    }

    private fun markAsApplied() {
        applyButton.text = "Already Applied"
        applyButton.isEnabled = false
        applyButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
    }

    private fun isJobSaved(): Boolean {
        val prefs = getUserPref("saved_jobs")
        return prefs.contains(getJobKey())
    }

    private fun toggleJobSave() {
        val prefs = getUserPref("saved_jobs")
        val editor = prefs.edit()
        val key = getJobKey()
        val isCurrentlySaved = prefs.contains(key)

        if (isCurrentlySaved) {
            editor.remove(key)
            Toast.makeText(this, "❌ Removed from saved jobs", Toast.LENGTH_SHORT).show()
        } else {
            val json = gson.toJson(job)
            editor.putString(key, json)
            Toast.makeText(this, "✅ Job saved!", Toast.LENGTH_SHORT).show()
        }

        editor.apply()
        updateSaveIcon(!isCurrentlySaved)
    }

    private fun updateSaveIcon(isSaved: Boolean) {
        job.isSaved = isSaved
        saveButton.setImageResource(
            if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
        )
    }
}
