package com.thuve.myrupees

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thuve.myrupees.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var balanceTextView: TextView
    private lateinit var WelcomeTextView: TextView

    // New function to get the current user from "user_preferences"
    private fun getCurrentUser(): String {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("current_user", "Guest") ?: "Guest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WelcomeTextView = findViewById(R.id.welcomeUser)
        WelcomeTextView.text = "Welcome, ${getCurrentUser()}"

        balanceTextView = findViewById(R.id.availableBalance)

        // Load only current user's transactions
        transactionList = SharedPrefManager.loadTransactions(this)
            .filter { it.user == getCurrentUser() }
            .toMutableList()

        val recyclerView = findViewById<RecyclerView>(R.id.transactionList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(
            transactions = transactionList,
            onDelete = {
                updateBalance()
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent("TRANSACTION_UPDATED"))
            },
            onEdit = { transaction, position ->
                showEditDialog(transaction, position)
            }
        )
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<TextView>(R.id.updatebal).setOnClickListener {
            showAddAmountDialog()
        }

        createNotificationChannel()
        scheduleRecurringNotificationWorker(this)
        requestNotificationPermission()
        updateBalance()
    }

    private fun showEditDialog(transaction: Transaction, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_transaction, null)

        dialogView.findViewById<EditText>(R.id.editTitle).setText(transaction.title)
        dialogView.findViewById<EditText>(R.id.editAmount).setText(transaction.amount.toString())
        dialogView.findViewById<EditText>(R.id.editCategory).setText(transaction.category)

        val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.typeRadioGroup)
        if (transaction.type == "Income") {
            typeRadioGroup.check(R.id.incomeRadio)
        } else {
            typeRadioGroup.check(R.id.expenseRadio)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Transaction")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.editTitle).text.toString()
                val amount = dialogView.findViewById<EditText>(R.id.editAmount).text.toString().toDoubleOrNull() ?: 0.0
                val category = dialogView.findViewById<EditText>(R.id.editCategory).text.toString()
                val type = if (typeRadioGroup.checkedRadioButtonId == R.id.incomeRadio) "Income" else "Expense"

                if (title.isBlank() || amount <= 0 || category.isBlank()) {
                    Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedTransaction = transaction.copy(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type
                )

                transactionList[position] = updatedTransaction
                SharedPrefManager.saveTransactions(this, SharedPrefManager.loadTransactions(this).map {
                    if (it.id == updatedTransaction.id) updatedTransaction else it
                })
                adapter.notifyItemChanged(position)
                updateBalance()
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent("TRANSACTION_UPDATED"))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddAmountDialog() {
        val editTextAmount = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(this)
            .setTitle("Add Amount")
            .setMessage("Please enter the amount:")
            .setView(editTextAmount)
            .setPositiveButton("Add") { _, _ ->
                val amount = editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    updateBalance(amount)
                    Toast.makeText(this, "Amount Added: Rs. $amount", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBalance() {
        val balance = transactionList
            .filter { it.user == getCurrentUser() }
            .sumOf {
                if (it.type == "Income") it.amount else -it.amount
            }
        balanceTextView.text = "Rs. %.2f".format(balance)
    }

    private fun updateBalance(amount: Double) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentBalance = transactionList
            .filter { it.user == getCurrentUser() }
            .sumOf {
                if (it.type == "Income") it.amount else -it.amount
            }
        val newBalance = currentBalance + amount

        val newTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Manual Add",
            amount = amount,
            category = "Manual",
            date = date,
            type = "Income",
            AvaiBal = newBalance,
            user = getCurrentUser()
        )

        val allTransactions = SharedPrefManager.loadTransactions(this).toMutableList()
        allTransactions.add(newTransaction)
        SharedPrefManager.saveTransactions(this, allTransactions)

        transactionList.add(newTransaction)
        adapter.notifyItemInserted(transactionList.size - 1)
        updateBalance()
    }

    override fun onResume() {
        super.onResume()
        val updatedTransactions = SharedPrefManager.loadTransactions(this)
            .filter { it.user == getCurrentUser() }
            .toMutableList()

        transactionList.clear()
        transactionList.addAll(updatedTransactions)
        adapter.notifyDataSetChanged()
        updateBalance()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MYRUPEES_CHANNEL_ID",
                "MyRupeesChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for MyRupees recurring notifications"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleRecurringNotificationWorker(context: Context) {
        val request = OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)

        val periodicRequest = OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.id).observe(this) { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                WorkManager.getInstance(context).enqueue(periodicRequest)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}