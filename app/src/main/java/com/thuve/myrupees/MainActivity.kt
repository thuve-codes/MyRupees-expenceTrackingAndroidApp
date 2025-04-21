package com.thuve.myrupees

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var balanceTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balanceTextView = findViewById(R.id.availableBalance)

        transactionList = SharedPrefManager.loadTransactions(this)

        val recyclerView = findViewById<RecyclerView>(R.id.transactionList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TransactionAdapter(transactionList) {
            updateBalance()
        }

        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.addBtn).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
            bottomNavigationView.selectedItemId = R.id.nav_add
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
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
                R.id.nav_recurring -> {
                    startActivity(Intent(this, RecurringTransactionActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val textViewAddAmount = findViewById<TextView>(R.id.updatebal)
        textViewAddAmount.setOnClickListener {
            val editTextAmount = EditText(this)
            editTextAmount.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            val dialogBuilder = AlertDialog.Builder(this)
                .setTitle("Add Amount")
                .setMessage("Please enter the amount:")
                .setView(editTextAmount)
                .setPositiveButton("Add") { _, _ ->
                    val amountString = editTextAmount.text.toString()
                    val amount = amountString.toDoubleOrNull() ?: 0.0

                    if (amount > 0) {
                        updateBalance(amount)
                        Toast.makeText(this, "Amount Added: Rs. $amount", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)

            dialogBuilder.create().show()
        }

        updateBalance()
    }

    private fun updateBalance() {
        val transactions = SharedPrefManager.loadTransactions(this)
        val balance = transactions.sumOf {
            if (it.type == "Income") it.amount else -it.amount
        }
        balanceTextView.text = "Rs. %.2f".format(balance)
    }

    private fun updateBalance(amount: Double) {
        // Get current date
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        // Calculate the new available balance based on existing transactions
        val currentBalance = transactionList.sumOf {
            if (it.type == "Income") it.amount else -it.amount
        }
        val newBalance = currentBalance + amount

        // Create new Transaction object with all required fields
        val newTransaction = Transaction(
            id = java.util.UUID.randomUUID().toString(),
            title = "Manual Add",
            amount = amount,
            category = "Manual", // You can customize this
            date = date,
            type = "Income",
            AvaiBal = newBalance
        )

        // Add transaction and save
        transactionList.add(newTransaction)
        SharedPrefManager.saveTransactions(this, transactionList)

        adapter.notifyDataSetChanged()
        updateBalance()
    }


    override fun onResume() {
        super.onResume()
        transactionList.clear()
        transactionList.addAll(SharedPrefManager.loadTransactions(this))
        adapter.notifyDataSetChanged()
        updateBalance()
    }

}
