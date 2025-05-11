package com.thuve.myrupeess


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thuve.myrupees.LoginActivity

import com.thuve.myrupees.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val sharedPrefFile = "user_preferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding correctly
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root) // This is the crucial fix

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.createbtn.setOnClickListener {
            attemptSignup()
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        val email = binding.sid.text.toString().trim()
        val username = binding.un.text.toString().trim()
        val password = binding.pwd.text.toString()
        val confirmPassword = binding.rpwd.text.toString()
        val termsAccepted = binding.terms.isChecked

        binding.Errortxt.text = ""

        if (!validateInput(email, username, password, confirmPassword, termsAccepted)) {
            return
        }

        saveUserCredentials(email, username, password)
    }

    private fun validateInput(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): Boolean {
        if (email.isEmpty()) {
            binding.Errortxt.text = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.Errortxt.text = "Please enter a valid email"
            return false
        }

        if (username.isEmpty()) {
            binding.Errortxt.text = "Username is required"
            return false
        }

        if (username.length < 4) {
            binding.Errortxt.text = "Username must be at least 4 characters"
            return false
        }

        if (password.isEmpty()) {
            binding.Errortxt.text = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.Errortxt.text = "Password must be at least 6 characters"
            return false
        }

        if (password != confirmPassword) {
            binding.Errortxt.text = "Passwords do not match"
            return false
        }

        if (!termsAccepted) {
            binding.Errortxt.text = "You must accept the terms and conditions"
            return false
        }

        return true
    }


    private fun saveUserCredentials(email: String, username: String, password: String) {
        val sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Check if email already exists
        val existingEmail = sharedPreferences.getString("email_$email", null)
        if (existingEmail != null) {
            binding.Errortxt.text = "Email already registered"
            return
        }

        // Store user data
        editor.putString("email_$email", email)
        editor.putString("username_$email", username)
        editor.putString("password_$email", password)
        editor.putBoolean("is_logged_in", false)
        editor.apply()

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}