package com.thuve.myrupees

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val titleInput = findViewById<EditText>(R.id.titleInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val categoryInput = findViewById<EditText>(R.id.categoryInput)
        val typeGroup = findViewById<RadioGroup>(R.id.typeGroup)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        // Set default color
        setSaveButtonColor(typeGroup, saveBtn)

        // Listener to update the button color based on type selection
        typeGroup.setOnCheckedChangeListener { _, checkedId ->
            setSaveButtonColor(typeGroup, saveBtn)
        }

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categoryInput.text.toString()
            val type = if (typeGroup.checkedRadioButtonId == R.id.incomeRadio) "Income" else "Expense"

            if (title.isEmpty() || amount == null || category.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                UUID.randomUUID().toString(),
                title,
                amount,
                category,
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                type
            )

            val transactions = SharedPrefManager.loadTransactions(this)
            transactions.add(transaction)
            SharedPrefManager.saveTransactions(this, transactions)

            finish()
        }

        // BottomNavigationView item selection listener
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Function to change the button color based on the transaction type
    private fun setSaveButtonColor(typeGroup: RadioGroup, saveBtn: Button) {
        when (typeGroup.checkedRadioButtonId) {
            R.id.incomeRadio -> {
                // Set the button color to green for income
                saveBtn.setBackgroundColor(resources.getColor(R.color.green))
            }
            R.id.expenseRadio -> {
                // Set the button color to red for expense
                saveBtn.setBackgroundColor(resources.getColor(R.color.red))
            }
        }
    }
}
