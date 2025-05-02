package com.example.nexthire

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var guestButton: Button
    private lateinit var signupButton: TextView

    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        guestButton = findViewById(R.id.guest_button)
        signupButton = findViewById(R.id.signup_button)

        dbHelper = UserDatabaseHelper(this)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val isValid = dbHelper.checkUser(email, password)
                if (isValid) {
                    // 🔐 Store email for personalized data usage
                    prefs.edit()
                        .putBoolean("isGuest", false)
                        .putBoolean("isLoggedIn", true)
                        .putString("email", email)
                        .apply()

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        guestButton.setOnClickListener {
            prefs.edit()
                .putBoolean("isGuest", true)
                .putBoolean("isLoggedIn", false)
                .remove("email") // ❌ No email = no personalized profile
                .apply()

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
