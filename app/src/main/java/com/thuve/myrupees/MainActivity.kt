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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

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

        findViewById<TextView>(R.id.updatebal).setOnClickListener {
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

        createNotificationChannel()
        scheduleRecurringNotificationWorker(this)
        requestNotificationPermission()
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
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val currentBalance = transactionList.sumOf {
            if (it.type == "Income") it.amount else -it.amount
        }
        val newBalance = currentBalance + amount

        val newTransaction = Transaction(
            id = java.util.UUID.randomUUID().toString(),
            title = "Manual Add",
            amount = amount,
            category = "Manual",
            date = date,
            type = "Income",
            AvaiBal = newBalance
        )

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MyRupeesChannel"
            val descriptionText = "Channel for MyRupees recurring notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MYRUPEES_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleRecurringNotificationWorker(context: Context) {
        val request = OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS) // Immediate first run
            .build()

        WorkManager.getInstance(context).enqueue(request)

        // Reschedule the worker every 3 seconds
        val periodicWorkRequest = OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()

        // Enqueue the worker with a custom retry mechanism to keep running every 3 seconds
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.id).observe(this) { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                // Reschedule every 3 seconds
                WorkManager.getInstance(context).enqueue(periodicWorkRequest)
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
