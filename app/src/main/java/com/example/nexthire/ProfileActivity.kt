package com.example.nexthire

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nexthire.data.JobRepository
import com.example.nexthire.data.ProfileStorage
import com.example.nexthire.model.Job
import com.example.nexthire.model.NotificationItem
import com.example.nexthire.utils.ImageUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private val RESUME_PICK_CODE = 1001
    private val IMAGE_PICK_CODE = 1002

    // UI
    private lateinit var nameInput: EditText
    private lateinit var titleInput: EditText
    private lateinit var aboutInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var skillsInput: EditText
    private lateinit var experienceInput: EditText
    private lateinit var jobTypeInput: EditText
    private lateinit var salaryInput: EditText
    private lateinit var resumeTextView: TextView
    private lateinit var uploadResumeBtn: Button
    private lateinit var saveProfileBtn: Button
    private lateinit var profileImageView: ImageView
    private lateinit var loginLogoutText: TextView
    private lateinit var editToggleBtn: Button

    private lateinit var currentUser: String
    private lateinit var storage: ProfileStorage
    private var selectedImageUri: Uri? = null

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix(applicationInfo.processName)
        }

        initializeViews()
        setupUserPreferences()
        loadProfileData()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        titleInput = findViewById(R.id.titleInput)
        aboutInput = findViewById(R.id.aboutInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        skillsInput = findViewById(R.id.skillsInput)
        experienceInput = findViewById(R.id.experienceInput)
        jobTypeInput = findViewById(R.id.jobTypeInput)
        salaryInput = findViewById(R.id.salaryInput)
        resumeTextView = findViewById(R.id.resumeFileText)
        uploadResumeBtn = findViewById(R.id.uploadResumeBtn)
        saveProfileBtn = findViewById(R.id.saveProfileBtn)
        profileImageView = findViewById(R.id.profileImageView)
        loginLogoutText = findViewById(R.id.loginLogoutText)
        editToggleBtn = findViewById(R.id.editToggleBtn)
    }

    private fun setupUserPreferences() {
        val userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUser = userPrefs.getString("email", "") ?: ""
        val isGuest = currentUser.isBlank()

        loginLogoutText.text = if (isGuest) "Login" else "Logout"

        loginLogoutText.setOnClickListener {
            if (isGuest) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit().remove("email").apply()
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        editToggleBtn.isEnabled = !isGuest
        saveProfileBtn.isEnabled = !isGuest
        uploadResumeBtn.isEnabled = !isGuest

        storage = ProfileStorage(this, if (isGuest) "guest" else currentUser)
    }

    private fun loadProfileData() {
        val saved = storage.loadProfile()
        nameInput.setText(saved["name"].orEmpty())
        titleInput.setText(saved["title"].orEmpty())
        aboutInput.setText(saved["about"].orEmpty())
        emailInput.setText(saved["email"].orEmpty())
        phoneInput.setText(saved["phone"].orEmpty())
        skillsInput.setText(saved["skills"].orEmpty())
        experienceInput.setText(saved["experience"].orEmpty())
        jobTypeInput.setText(saved["jobType"].orEmpty())
        salaryInput.setText(saved["salary"].orEmpty())

        val path = storage.getExtra("profileImagePath")
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists()) {
                profileImageView.setImageURI(Uri.fromFile(file))
            } else {
                Toast.makeText(this, "⚠️ Image file not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        uploadResumeBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
            startActivityForResult(Intent.createChooser(intent, "Select Resume"), RESUME_PICK_CODE)
        }

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), IMAGE_PICK_CODE)
        }

        saveProfileBtn.setOnClickListener { saveProfileAndFindMatches() }

        editToggleBtn.setOnClickListener {
            val isEnabled = !nameInput.isEnabled
            listOf(nameInput, titleInput, aboutInput, emailInput, phoneInput, skillsInput, experienceInput, jobTypeInput, salaryInput).forEach {
                it.isEnabled = isEnabled
            }
            uploadResumeBtn.isEnabled = isEnabled
            saveProfileBtn.isEnabled = isEnabled
            editToggleBtn.text = if (isEnabled) "Cancel" else "Edit Profile"
        }
    }

    private fun saveProfileAndFindMatches() {
        val profile = mapOf(
            "name" to nameInput.text.toString(),
            "title" to titleInput.text.toString(),
            "about" to aboutInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phone" to phoneInput.text.toString(),
            "skills" to skillsInput.text.toString(),
            "experience" to experienceInput.text.toString(),
            "jobType" to jobTypeInput.text.toString(),
            "salary" to salaryInput.text.toString()
        )
        storage.saveProfile(profile)

        selectedImageUri?.let {
            val path = ImageUtils.saveImageToInternalStorage(this, it)
            if (path != null) {
                storage.saveExtra("profileImagePath", path)
            } else {
                Toast.makeText(this, "❌ Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }

        showToast("Profile saved successfully!")

        // ✅ Disable editing after save
        val inputs = listOf(
            nameInput, titleInput, aboutInput, emailInput, phoneInput,
            skillsInput, experienceInput, jobTypeInput, salaryInput
        )
        inputs.forEach { it.isEnabled = false }
        uploadResumeBtn.isEnabled = false
        saveProfileBtn.isEnabled = false
        editToggleBtn.text = "Edit Profile"

        val matchedJobs = recommendJobs(
            profile["skills"].orEmpty(),
            profile["experience"].orEmpty(),
            profile["jobType"].orEmpty(),
            profile["salary"].orEmpty(),
            profile["title"].orEmpty()
        )

        val userEmail = profile["email"] ?: "guest@demo.com"
        val existingIds = NotificationStorage.loadAll(this, userEmail).map { it.jobId }.toSet()

        val newNotifications = matchedJobs.filter { it.id !in existingIds }.map { job ->
            NotificationItem(
                message = "🎯 ${job.title} at ${job.company} — ${job.salary}",
                time = getCurrentTime(),
                jobTitle = job.title,
                jobCompany = job.company,
                jobId = job.id
            )
        }

        if (newNotifications.isNotEmpty()) {
            newNotifications.forEach { NotificationStorage.append(this, it, userEmail) }
            val intent = Intent(this, NotificationActivity::class.java)
            intent.putExtra("matches", ArrayList(newNotifications.map { it.message }))
            startActivity(intent)
        } else {
            showToast("No new matching jobs found.")
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun recommendJobs(
        userSkills: String,
        experience: String,
        jobType: String,
        expectedSalary: String,
        expectedTitle: String
    ): List<Job> {
        val userExperience = extractYears(experience)
        val userExpectedSalary = extractSalary(expectedSalary)

        return JobRepository.jobs.filter { job ->
            var score = 0
            val titleMatch = expectedTitle.isBlank() || job.title.contains(expectedTitle, ignoreCase = true)
            if (!titleMatch) return@filter false
            if (userSkills.isNotBlank() && job.description.contains(userSkills, ignoreCase = true)) score++
            val jobExp = extractYears(job.experience)
            if (userExperience != null && jobExp != null && userExperience >= jobExp) score++
            if (jobType.isNotBlank() && job.type.equals(jobType, ignoreCase = true)) score++
            val (minSalary, maxSalary) = extractSalaryRange(job.salary)
            if (userExpectedSalary != null && minSalary != null && maxSalary != null &&
                userExpectedSalary in minSalary..maxSalary
            ) score++
            score >= 1
        }
    }

    private fun extractYears(text: String?): Int? {
        return Regex("(\\d+)\\+?\\s*years?", RegexOption.IGNORE_CASE)
            .find(text ?: "")?.groups?.get(1)?.value?.toIntOrNull()
    }

    private fun extractSalary(text: String?): Int? {
        return Regex("\\$?(\\d+)[kK]").find(text ?: "")?.groups?.get(1)?.value?.toIntOrNull()?.times(1000)
    }

    private fun extractSalaryRange(salary: String?): Pair<Int?, Int?> {
        val matches = Regex("\\$?(\\d+)[kK]").findAll(salary ?: "").toList()
        val nums = matches.mapNotNull { it.groups[1]?.value?.toIntOrNull()?.times(1000) }
        return Pair(nums.getOrNull(0), nums.getOrNull(1))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data!!
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    selectedImageUri = uri
                    profileImageView.setImageURI(uri)
                }
                RESUME_PICK_CODE -> {
                    resumeTextView.text = getFileNameFromUri(uri)
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_profile
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
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
