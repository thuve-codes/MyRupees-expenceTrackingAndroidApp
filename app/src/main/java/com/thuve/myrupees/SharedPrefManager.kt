package com.thuve.myrupees

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {
    private const val PREF_NAME = "myrupees_prefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private val gson = Gson()

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_TRANSACTIONS, gson.toJson(transactions))
        }
    }

    fun loadTransactions(context: Context): MutableList<Transaction> {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRANSACTIONS, null)
            ?.let { json ->
                gson.fromJson(json, object : TypeToken<MutableList<Transaction>>() {}.type)
            } ?: mutableListOf()
    }
}
