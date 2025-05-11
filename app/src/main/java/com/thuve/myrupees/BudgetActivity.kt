package com.thuve.myrupees

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var etBudgetAmount: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var tvCurrentBudget: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private val PREFS_NAME = "myrupees_prefs"
    private val KEY_MONTH = "saved_month"

    private val transactionUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshExpenses()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        etBudgetAmount = findViewById(R.id.etBudgetAmount)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget)
        tvExpenses = findViewById(R.id.tvExpenses)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        createNotificationChannel()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Reset budget monthly for the current user
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val savedMonth = prefs.getInt("${KEY_MONTH}_${getCurrentUser()}", -1)
        if (savedMonth != currentMonth) {
            editor.putFloat("budget_${getCurrentUser()}", 0f)
            editor.putInt("${KEY_MONTH}_${getCurrentUser()}", currentMonth)
            editor.apply()
        }

        // Bottom navigation setup
        bottomNavigationView.selectedItemId = R.id.nav_budget
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val view = bottomNavigationView.findViewById<View>(menuItem.itemId)
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 150
            }.start()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_budget -> true
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.nav_recurring -> {
                    startActivity(Intent(this, RecurringTransactionActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        btnSaveBudget.setOnClickListener {
            val budgetInput = etBudgetAmount.text.toString().trim()
            val newBudget = budgetInput.toDoubleOrNull()

            if (newBudget != null && newBudget > 0) {
                editor.putFloat("budget_${getCurrentUser()}", newBudget.toFloat()).apply()
                etBudgetAmount.text.clear()
                refreshExpenses(newBudget)

                val remaining = newBudget - calculateTotalExpenses()
                val status = if (remaining >= 0)
                    "✅ Budget saved! Remaining: Rs %.2f".format(remaining)
                else
                    "⚠️ Over Budget by: Rs %.2f".format(-remaining)

                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            } else {
                etBudgetAmount.error = "❌ Please enter a valid amount"
            }
        }

        refreshExpenses()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(transactionUpdateReceiver, IntentFilter("TRANSACTION_UPDATED"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(transactionUpdateReceiver)
    }

    override fun onResume() {
        super.onResume()
        refreshExpenses()
    }

    private fun getCurrentUser(): String {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("current_user", "Guest") ?: "Guest"
    }

    private fun refreshExpenses(budget: Double? = null) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentBudget = budget ?: prefs.getFloat("budget_${getCurrentUser()}", 0f).toDouble()
        val totalExpenses = calculateTotalExpenses()

        tvExpenses.text = "Total Expenses: Rs %.2f".format(totalExpenses)

        if (currentBudget > 0) {
            tvCurrentBudget.text = "Current Budget: Rs %.2f".format(currentBudget)
            updateProgress(currentBudget, totalExpenses)
        } else {
            tvCurrentBudget.text = "Current Budget: Not Set"
            budgetProgressBar.progress = 0
            tvProgressPercent.text = "0% Spent"
        }
    }

    private fun calculateTotalExpenses(): Double {
        val transactions = SharedPrefManager.loadTransactions(this)
        return transactions
            .filter { it.type == "Expense" && it.user == getCurrentUser() }
            .sumOf { it.amount }
            .coerceAtLeast(0.0)
    }

    private fun updateProgress(budget: Double, expenses: Double) {
        val percentSpent = ((expenses / budget) * 100).toInt()
        val displayPercent = percentSpent.coerceAtMost(100)

        budgetProgressBar.progress = displayPercent
        tvProgressPercent.text = if (percentSpent > 100) {
            "100%+ Spent"
        } else {
            "$displayPercent% Spent"
        }

        if (percentSpent in 90..99) {
            NotificationHelper.showNotification(
                this,
                "⚠️ Budget Alert",
                "You've spent over 90% of your budget. Monitor your expenses carefully."
            )
        } else if (percentSpent >= 100) {
            NotificationHelper.showNotification(
                this,
                "🚨 Budget Exceeded",
                "You have exceeded your budget by Rs %.2f!".format(expenses - budget)
            )
            Toast.makeText(this, "⚠️ You have exceeded your budget!", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "budget_channel",
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when budget usage is high or exceeded"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}