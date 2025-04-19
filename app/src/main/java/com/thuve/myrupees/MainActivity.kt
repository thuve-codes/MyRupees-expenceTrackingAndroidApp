package com.thuve.myrupees

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load transactions
        transactionList = SharedPrefManager.loadTransactions(this)

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.transactionList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(transactionList)
        recyclerView.adapter = adapter

        // Add transaction button
        findViewById<FloatingActionButton>(R.id.addBtn).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
            bottomNavigationView.selectedItemId = R.id.nav_add
        }

        // Setup BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val view = bottomNavigationView.findViewById<View>(menuItem.itemId)
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 150
            }.start()

            when (menuItem.itemId) {

                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionActivity::class.java))
                    true
                }

                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                }
                R.id.nav_categories -> {
                    Toast.makeText(this, "Categories", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        transactionList.clear()
        transactionList.addAll(SharedPrefManager.loadTransactions(this))
        adapter.notifyDataSetChanged()
    }
}
