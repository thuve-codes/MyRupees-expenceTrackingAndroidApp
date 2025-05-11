package com.thuve.myrupees

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class RecurringTransactionActivity : AppCompatActivity() {

    private lateinit var upcomingRecycler: RecyclerView
    private lateinit var recentRecycler: RecyclerView
    private lateinit var titleInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var startDateInput: EditText
    private lateinit var monthsInput: EditText
    private lateinit var bottomNavigationView: BottomNavigationView

    private val allTransactions = mutableListOf<RecurringTransaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recurring_transactions)

        // Initialize views
        titleInput = findViewById(R.id.titleInput)
        amountInput = findViewById(R.id.amountInput)
        startDateInput = findViewById(R.id.startDateInput)
        monthsInput = findViewById(R.id.monthsInput)
        val addBtn = findViewById<Button>(R.id.addRecurringBtn)
        upcomingRecycler = findViewById(R.id.upcomingRecycler)
        recentRecycler = findViewById(R.id.recentRecycler)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Bottom navigation
        bottomNavigationView.selectedItemId = R.id.nav_recurring
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
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Load saved transactions for the current user
        allTransactions.addAll(
            SharedPrefManager.loadRecurringTransactions(this)
                .filter { it.user == getCurrentUser() }
        )
        setupAdapters()

        // Show date picker
        startDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    startDateInput.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Add new recurring transactions
        addBtn.setOnClickListener {
            try {
                val title = titleInput.text.toString()
                val amount = amountInput.text.toString().toDoubleOrNull()
                val date = startDateInput.text.toString()
                val months = monthsInput.text.toString().toIntOrNull()

                if (title.isBlank()) {
                    toast("Title cannot be empty")
                    return@setOnClickListener
                }
                if (amount == null) {
                    toast("Please enter a valid amount")
                    return@setOnClickListener
                }
                if (months == null || months <= 0) {
                    toast("Please enter a valid number of months")
                    return@setOnClickListener
                }
                if (date.isBlank()) {
                    toast("Please select a start date")
                    return@setOnClickListener
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = sdf.parse(date) ?: throw IllegalArgumentException("Invalid date format")
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate

                repeat(months) {
                    val recurring = RecurringTransaction(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        amount = amount,
                        scheduledDate = sdf.format(calendar.time),
                        paid = false,
                        user = getCurrentUser()
                    )
                    allTransactions.add(recurring)
                    calendar.add(Calendar.MONTH, 1)
                }

                // Save all transactions, including those from other users
                val allRecurringTransactions = SharedPrefManager.loadRecurringTransactions(this)
                    .filter { it.user != getCurrentUser() }
                    .toMutableList()
                allRecurringTransactions.addAll(allTransactions)
                SharedPrefManager.saveRecurringTransactions(this, allRecurringTransactions)
                setupAdapters()

                titleInput.text.clear()
                amountInput.text.clear()
                startDateInput.text.clear()
                monthsInput.text.clear()

                toast("Transactions added successfully")

            } catch (e: Exception) {
                Log.e("RecurringTransaction", "Error: ${e.message}")
                toast("Error: ${e.message}")
            }
        }
    }

    private fun getCurrentUser(): String {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("current_user", "Guest") ?: "Guest"
    }

    private fun setupAdapters() {
        val upcoming = allTransactions.filter { !it.paid }
        val recent = allTransactions.filter { it.paid }

        upcomingRecycler.layoutManager = LinearLayoutManager(this)
        recentRecycler.layoutManager = LinearLayoutManager(this)

        upcomingRecycler.adapter = RecurringAdapter(upcoming.toMutableList()) { paidItem ->
            val index = allTransactions.indexOfFirst { it.id == paidItem.id }
            if (index != -1) {
                allTransactions[index] = allTransactions[index].copy(paid = true)
                // Save all transactions, preserving those from other users
                val allRecurringTransactions = SharedPrefManager.loadRecurringTransactions(this)
                    .filter { it.user != getCurrentUser() }
                    .toMutableList()
                allRecurringTransactions.addAll(allTransactions)
                SharedPrefManager.saveRecurringTransactions(this, allRecurringTransactions)
                setupAdapters()
            }
        }

        recentRecycler.adapter = RecurringAdapter(recent.toMutableList(), null)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}