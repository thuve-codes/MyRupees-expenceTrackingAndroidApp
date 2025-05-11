package com.thuve.myrupees

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val titleInput = findViewById<EditText>(R.id.titleInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val typeGroup = findViewById<RadioGroup>(R.id.typeGroup)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val exportBtn = findViewById<Button>(R.id.exportButton)

        // Set up category spinner
        val categories = listOf("Food", "Transport", "Entertainment", "Shopping", "Savings", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        setSaveButtonColor(typeGroup, saveBtn)
        typeGroup.setOnCheckedChangeListener { _, _ -> setSaveButtonColor(typeGroup, saveBtn) }

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val type = if (typeGroup.checkedRadioButtonId == R.id.incomeRadio) "Income" else "Expense"

            if (title.isEmpty() || amount == null || category.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transactions = SharedPrefManager.loadTransactions(this)
            val currentBalance = transactions.sumOf { if (it.type == "Income") it.amount else -it.amount }
            val updatedBalance = if (type == "Income") currentBalance + amount else currentBalance - amount

            // Create transaction with the correct username
            val transaction = Transaction(
                UUID.randomUUID().toString(),
                title,
                amount,
                category,
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                type,
                updatedBalance,
                getCurrentUser()
            )

            transactions.add(transaction)
            SharedPrefManager.saveTransactions(this, transactions)

            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Export and share functionality
        exportBtn.setOnClickListener {
            val transactions = SharedPrefManager.loadTransactions(this)

            if (transactions.isEmpty()) {
                Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to Downloads folder using MediaStore for Android 10+ (Scoped Storage)
            val fileName = "MyRupees_Transactions.txt"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_DOWNLOADS}")
            }

            val contentResolver = contentResolver
            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                try {
                    val outputStream = contentResolver.openOutputStream(it)
                    outputStream?.use { writer ->
                        writer.write("ID, Title, Amount, Category, Date, Type, Available Balance\n".toByteArray())
                        for (t in transactions) {
                            writer.write("${t.id}, ${t.title}, ${t.amount}, ${t.category}, ${t.date}, ${t.type}, ${t.AvaiBal}\n".toByteArray())
                        }
                    }

                    Toast.makeText(this, "Transactions exported to Downloads", Toast.LENGTH_SHORT).show()

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_STREAM, it)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Share transactions via"))
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Bottom navigation
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun getCurrentUser(): String {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("current_user", "Guest") ?: "Guest"
    }

    private fun setSaveButtonColor(typeGroup: RadioGroup, saveBtn: Button) {
        when (typeGroup.checkedRadioButtonId) {
            R.id.incomeRadio -> saveBtn.setBackgroundColor(resources.getColor(R.color.green))
            R.id.expenseRadio -> saveBtn.setBackgroundColor(resources.getColor(R.color.red))
        }
    }
}