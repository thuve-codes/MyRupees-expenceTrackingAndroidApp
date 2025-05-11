package com.thuve.myrupees

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thuve.myrupees.databinding.ActivityLoginBinding
import com.thuve.myrupeess.SignupActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val sharedPrefFile = "user_preferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root) // ✅ Use binding.root here!

        setupClickListeners()
        checkRememberedUser()
    }

    private fun setupClickListeners() {
        binding.loginbtn.setOnClickListener {
            attemptLogin()
        }

        binding.regbtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun checkRememberedUser() {
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            navigateToMainActivity()
        }
    }

    private fun attemptLogin() {
        val username = binding.entersid.text.toString().trim()
        val password = binding.enterpassword.text.toString()

        binding.error.text = "" // Clear previous error

        if (username.isEmpty()) {
            binding.error.text = "Username is required"
            return
        }

        if (password.isEmpty()) {
            binding.error.text = "Password is required"
            return
        }

        authenticateUser(username, password)
    }

    private fun authenticateUser(username: String, password: String) {
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        var userFound = false

        for ((key, value) in allEntries) {
            if (key.startsWith("username_") && value == username) {
                userFound = true
                val email = key.removePrefix("username_")
                val storedPassword = sharedPreferences.getString("password_$email", "")

                if (password == storedPassword) {
                    // Successful login
                    sharedPreferences.edit().apply {
                        putBoolean("is_logged_in", true)
                        putString("current_user", username)
                        apply()
                    }
                    navigateToMainActivity()
                    return
                } else {
                    binding.error.text = "Incorrect password"
                    return
                }
            }
        }

        if (!userFound) {
            binding.error.text = "Username not found"
        }
    }

    private fun navigateToMainActivity() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            finishAffinity() // Prevent going back if logged in
        } else {
            super.onBackPressed()
        }
    }
}
