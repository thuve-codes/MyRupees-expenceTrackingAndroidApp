package com.thuve.myrupees

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {
    private const val PREF_NAME = "finance_tracker"
    private const val KEY_TRANSACTIONS = "transactions"

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(transactions)
        editor.putString(KEY_TRANSACTIONS, json)
        editor.apply()
    }

    fun loadTransactions(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        return if (json != null) Gson().fromJson(json, type) else mutableListOf()
    }
}
