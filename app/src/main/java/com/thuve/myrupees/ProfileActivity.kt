package com.thuve.myrupees

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etFeedback: EditText
    private lateinit var btnSignOut: Button

    private val sharedPrefFile = "user_preferences" // ✅ Match with LoginActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName)
        tvEmail = findViewById(R.id.tvEmail)
        etFeedback = findViewById(R.id.etFeedback)
        btnSignOut = findViewById(R.id.btnSignOut)

        // Load user data
        val sharedPref = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val username = sharedPref.getString("current_user", "Guest")
        var email = "no-email@example.com"

        // Try to extract email using username
        sharedPref.all.forEach { (key, value) ->
            if (key.startsWith("username_") && value == username) {
                val matchedEmail = key.removePrefix("username_")
                email = matchedEmail
            }
        }

        tvUserName.text = username
        tvEmail.text = email

        // Sign out logic
        btnSignOut.setOnClickListener {
            sharedPref.edit().clear().apply()

            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
