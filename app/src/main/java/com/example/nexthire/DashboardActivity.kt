package com.example.nexthire

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    companion object {
        var currentUserEmail: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 📨 Get user and nav flags
        currentUserEmail = intent.getStringExtra("user_email")
        val navigateTo = intent.getStringExtra("navigateTo")

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // 🧭 Navigate to requested fragment
        when (navigateTo) {
            "saved" -> {
                loadFragment(SavedFragment())
                bottomNav.selectedItemId = R.id.nav_saved
            }
            "applied" -> {
                loadFragment(MyApplicationsFragment())
                bottomNav.selectedItemId = R.id.nav_applied
            }
            "profile" -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                bottomNav.selectedItemId = R.id.nav_profile
            }
            "notifications" -> {
                startActivity(Intent(this, NotificationActivity::class.java))
                bottomNav.selectedItemId = R.id.nav_notifications
            }
            else -> {
                loadFragment(HomeFragment())
                bottomNav.selectedItemId = R.id.nav_home
            }
        }



        // ⛴ Handle tab switch
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_saved -> {
                    loadFragment(SavedFragment())
                    true
                }
                R.id.nav_applied -> {
                    loadFragment(MyApplicationsFragment())
                    true
                }
                R.id.nav_notifications -> {
                // ✅ YOUR NOTIFICATION LOGIC
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("navigateTo", "notifications")
                startActivity(intent)
                true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}
