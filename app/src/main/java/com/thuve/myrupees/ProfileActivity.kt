package com.thuve.myrupees

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etFeedback: EditText
    private lateinit var btnSignOut: Button
    private lateinit var btnSendFeedback: Button
    private lateinit var feedbackContainer: LinearLayout

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // View references
        tvUserName = findViewById(R.id.tvUserName)
        tvEmail = findViewById(R.id.tvEmail)
        etFeedback = findViewById(R.id.etFeedback)
        btnSignOut = findViewById(R.id.btnSignOut)
        btnSendFeedback = findViewById(R.id.btnSendFeedback)
        feedbackContainer = findViewById(R.id.feedbackContainer)

        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_user", "Guest") ?: "Guest"
        val email = sharedPref.getString("email_$currentUsername", "no-email@example.com")

        tvUserName.text = currentUsername
        tvEmail.text = email

        // Send feedback and append it for the current user
        btnSendFeedback.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            if (feedback.isNotEmpty()) {
                // Get the existing feedbacks for the current user, or initialize an empty string if none exist
                val existingFeedback = sharedPref.getString("feedback_$currentUsername", "")

                // Append the new feedback to the existing one (if any)
                val updatedFeedback = if (existingFeedback.isNullOrEmpty()) {
                    feedback
                } else {
                    "$existingFeedback\n\n$feedback" // Separate feedbacks with a newline
                }

                // Save updated feedback for the current user
                sharedPref.edit().putString("feedback_$currentUsername", updatedFeedback).apply()

                Toast.makeText(this, "Feedback saved!", Toast.LENGTH_SHORT).show()
                loadAllFeedbacks(sharedPref) // Reload updated feedback list
                etFeedback.text.clear() // Clear the input field after saving
            } else {
                Toast.makeText(this, "Please enter feedback.", Toast.LENGTH_SHORT).show()
            }
        }

        // Sign out
        btnSignOut.setOnClickListener {
            sharedPref.edit().apply {
                putBoolean("is_logged_in", false)
                remove("current_user")
            }.apply()

            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Load all feedbacks from all users
        loadAllFeedbacks(sharedPref)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val view = bottomNavigationView.findViewById<View>(menuItem.itemId)
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 150
            }.start()

            when (menuItem.itemId) {
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.nav_recurring -> {
                    startActivity(Intent(this, RecurringTransactionActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }


    }

    private fun loadAllFeedbacks(sharedPref: SharedPreferences) {
        feedbackContainer.removeAllViews()

        // Iterate through all keys in SharedPreferences
        for ((key, value) in sharedPref.all) {
            if (key.startsWith("feedback_")) { // Look for feedback keys
                // Get the username from the feedback key, assuming the format is "feedback_username"
                val username = key.removePrefix("feedback_")
                val feedback = value.toString()

                // Create a TextView for each feedback
                val feedbackText = TextView(this).apply {
                    text = "$username: $feedback" // Ensure username appears before feedback
                    setTextColor(resources.getColor(R.color.black))
                    textSize = 16f
                    setPadding(16, 8, 16, 16)

                    setTypeface(null, android.graphics.Typeface.BOLD) // Make the username bold
                }

                // Add the TextView to the feedback container
                feedbackContainer.addView(feedbackText)
            }
        }
    }




}
