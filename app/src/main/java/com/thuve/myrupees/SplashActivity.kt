package com.thuve.myrupees

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val getStartedButton: Button = findViewById(R.id.getstart)
        getStartedButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Optional: Finish SplashActivity so it's removed from the back stack
        }
    }
}
