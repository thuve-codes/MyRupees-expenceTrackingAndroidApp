package com.thuve.myrupees

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val titleInput = findViewById<EditText>(R.id.titleInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val typeGroup = findViewById<RadioGroup>(R.id.typeGroup)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        // Set up category options for Spinner
        val categories = listOf("Food", "Transport", "Entertainment", "Shopping", "Savings", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Set default color
        setSaveButtonColor(typeGroup, saveBtn)

        // Change button color when income/expense changes
        typeGroup.setOnCheckedChangeListener { _, _ ->
            setSaveButtonColor(typeGroup, saveBtn)
        }

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val type = if (typeGroup.checkedRadioButtonId == R.id.incomeRadio) "Income" else "Expense"

            if (title.isEmpty() || amount == null || category.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Load existing transactions
            val transactions = SharedPrefManager.loadTransactions(this)

            // Calculate current balance
            var currentBalance = 0.0
            for (t in transactions) {
                currentBalance += if (t.type == "Income") t.amount else -t.amount
            }

            // Update balance based on new transaction
            val updatedBalance = if (type == "Income") {
                currentBalance + amount
            } else {
                currentBalance - amount
            }

            // Create and save transaction
            val transaction = Transaction(
                UUID.randomUUID().toString(),
                title,
                amount,
                category,
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                type,
                updatedBalance
            )

            transactions.add(transaction)
            SharedPrefManager.saveTransactions(this, transactions)

            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // BottomNavigationView item selection
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_add
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }

                R.id.nav_recurring -> {
                    startActivity(Intent(this, RecurringTransactionActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    // Change Save Button color depending on type
    private fun setSaveButtonColor(typeGroup: RadioGroup, saveBtn: Button) {
        when (typeGroup.checkedRadioButtonId) {
            R.id.incomeRadio -> {
                saveBtn.setBackgroundColor(resources.getColor(R.color.green))
            }
            R.id.expenseRadio -> {
                saveBtn.setBackgroundColor(resources.getColor(R.color.red))
            }
        }
    }
}
