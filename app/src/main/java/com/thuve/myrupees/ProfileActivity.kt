package com.thuve.myrupees

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etFeedback: EditText
    private lateinit var btnSignOut: Button
    private lateinit var btnSendFeedback: Button
    private lateinit var feedbackContainer: LinearLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: TransactionViewModel by viewModels { TransactionViewModelFactory(this, getCurrentUser()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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

        btnSendFeedback.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            if (feedback.isNotEmpty()) {
                viewModel.insertFeedback(Feedback(user = currentUsername, feedback = feedback, timestamp = System.currentTimeMillis()))
                Toast.makeText(this, "Feedback saved!", Toast.LENGTH_SHORT).show()
                etFeedback.text.clear()
            } else {
                Toast.makeText(this, "Please enter feedback.", Toast.LENGTH_SHORT).show()
            }
        }

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

        lifecycleScope.launch {
            viewModel.allFeedbacks.collectLatest { feedbacks ->
                feedbackContainer.removeAllViews()
                feedbacks.forEach { feedback ->
                    val feedbackText = TextView(this@ProfileActivity).apply {
                        text = "${feedback.user}: ${feedback.feedback}"
                        setTextColor(resources.getColor(R.color.black))
                        textSize = 16f
                        setPadding(16, 8, 16, 16)
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    feedbackContainer.addView(feedbackText)
                }
            }
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val view = bottomNavigationView.findViewById<View>(menuItem.itemId)
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 150
            }.start()

            when (menuItem.itemId) {
                R.id.nav_budget -> { startActivity(Intent(this, BudgetActivity::class.java)); true }
                R.id.nav_add -> { startActivity(Intent(this, AddTransactionActivity::class.java)); true }
                R.id.nav_recurring -> { startActivity(Intent(this, RecurringTransactionActivity::class.java)); true }
                R.id.nav_home -> { startActivity(Intent(this, MainActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun getCurrentUser(): String {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("current_user", "Guest") ?: "Guest"
    }
}