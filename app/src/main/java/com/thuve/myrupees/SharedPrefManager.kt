package com.thuve.myrupees

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {
    private const val PREF_NAME = "myrupees_prefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_BUDGET = "budget"
    private const val KEY_NOTIFICATION_90 = "notified_90_percent"
    private const val KEY_NOTIFICATION_100 = "notified_100_percent"
    private const val KEY_RECURRING_UPCOMING = "recurring_upcoming"
    private const val KEY_RECURRING_RECENT = "recurring_recent"
    private val gson = Gson()

    // Save transactions to SharedPreferences
    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            val json = gson.toJson(transactions)
            Log.d("SharedPrefManager", "Saving transactions JSON: $json")
            putString(KEY_TRANSACTIONS, json)
        }
    }

    // Load transactions from SharedPreferences
    fun loadTransactions(context: Context): MutableList<Transaction> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRANSACTIONS, null)
        Log.d("SharedPrefManager", "Loaded transactions JSON: $json")
        return json?.let {
            gson.fromJson(it, object : TypeToken<MutableList<Transaction>>() {}.type)
        } ?: mutableListOf()
    }

    // Save budget amount
    fun setBudget(context: Context, budget: Double) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putFloat(KEY_BUDGET, budget.toFloat()) // Use Float for SharedPreferences
            Log.d("SharedPrefManager", "Saving budget: $budget")
        }
    }

    // Retrieve budget amount
    fun getBudget(context: Context): Double {
        val budget = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_BUDGET, 0f).toDouble()
        Log.d("SharedPrefManager", "Loaded budget: $budget")
        return budget
    }

    // Save notification state (for 90% or 100% threshold)
    fun saveNotificationState(context: Context, key: String, state: Boolean) {
        val prefKey = when (key) {
            "90Percent" -> KEY_NOTIFICATION_90
            "100Percent" -> KEY_NOTIFICATION_100
            else -> return // Invalid key
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(prefKey, state)
            Log.d("SharedPrefManager", "Saving notification state: $prefKey = $state")
        }
    }

    // Retrieve notification state
    fun getNotificationState(context: Context, key: String): Boolean {
        val prefKey = when (key) {
            "90Percent" -> KEY_NOTIFICATION_90
            "100Percent" -> KEY_NOTIFICATION_100
            else -> return false // Invalid key
        }
        val state = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(prefKey, false)
        Log.d("SharedPrefManager", "Loaded notification state: $prefKey = $state")
        return state
    }
    private const val KEY_RECURRING_TRANSACTIONS = "recurring_transactions"

    fun saveRecurring(context: Context, transactions: List<RecurringTransaction>) {
        val json = gson.toJson(transactions)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_RECURRING_TRANSACTIONS, json)
        }
    }

    fun loadRecurring(context: Context): MutableList<RecurringTransaction> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RECURRING_TRANSACTIONS, null)
        return json?.let {
            gson.fromJson(it, object : TypeToken<MutableList<RecurringTransaction>>() {}.type)
        } ?: mutableListOf()
    }

}