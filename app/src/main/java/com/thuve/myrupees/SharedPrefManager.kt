package com.thuve.myrupees

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {
    private const val PREF_NAME = "myrupees_prefs"
    private const val KEY_TRANSACTIONS = "transactions"

    /* private const val KEY_BUDGET = "budget"
     private const val KEY_NOTIFICATION_90 = "notified_90_percent"
     private const val KEY_NOTIFICATION_100 = "notified_100_percent"*/

    private const val KEY_RECURRING_TRANSACTIONS = "recurring_transactions"

    private val gson = Gson()

    // --------------------- Transactions ---------------------
    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_TRANSACTIONS, json)
            Log.d("SharedPrefManager", "Saving transactions JSON: $json")
        }
    }

    fun loadTransactions(context: Context): MutableList<Transaction> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRANSACTIONS, null)
        Log.d("SharedPrefManager", "Loaded transactions JSON: $json")
        return json?.let {
            gson.fromJson(it, object : TypeToken<MutableList<Transaction>>() {}.type)
        } ?: mutableListOf()
    }




    /*
        // --------------------- Budget ---------------------
        fun setBudget(context: Context, budget: Double) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
                putFloat(KEY_BUDGET, budget.toFloat())
                Log.d("SharedPrefManager", "Saving budget: $budget")
            }
        }

        fun getBudget(context: Context): Double {
            val budget = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getFloat(KEY_BUDGET, 0f).toDouble()
            Log.d("SharedPrefManager", "Loaded budget: $budget")
            return budget
        }

        // --------------------- Notification State ---------------------
        fun saveNotificationState(context: Context, key: String, state: Boolean) {
            val prefKey = when (key) {
                "90Percent" -> KEY_NOTIFICATION_90
                "100Percent" -> KEY_NOTIFICATION_100
                else -> return
            }
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
                putBoolean(prefKey, state)
                Log.d("SharedPrefManager", "Saving notification state: $prefKey = $state")
            }
        }

        fun getNotificationState(context: Context, key: String): Boolean {
            val prefKey = when (key) {
                "90Percent" -> KEY_NOTIFICATION_90
                "100Percent" -> KEY_NOTIFICATION_100
                else -> return false
            }
            val state = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(prefKey, false)
            Log.d("SharedPrefManager", "Loaded notification state: $prefKey = $state")
            return state
        }
    */





    // --------------------- Recurring Transactions ---------------------
    fun saveRecurringTransactions(context: Context, transactions: List<RecurringTransaction>) {
        val json = gson.toJson(transactions)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_RECURRING_TRANSACTIONS, json)
            Log.d("SharedPrefManager", "Saving recurring transactions: $json")
        }
    }

    fun loadRecurringTransactions(context: Context): MutableList<RecurringTransaction> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RECURRING_TRANSACTIONS, null)
        Log.d("SharedPrefManager", "Loaded recurring transactions JSON: $json")
        return json?.let {
            gson.fromJson(it, object : TypeToken<MutableList<RecurringTransaction>>() {}.type)
        } ?: mutableListOf()
    }

    fun getCurrentUsername(context: Context): String {
        // Retrieve the username from shared preferences
        val username = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("username", "Guest") // Defaulting to "Guest" if not found
        Log.d("SharedPrefManager", "Loaded username: $username")
        return username ?: "Guest" // In case the username is null
    }

}