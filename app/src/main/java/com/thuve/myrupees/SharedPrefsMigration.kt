package com.thuve.myrupees

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

suspend fun migrateSharedPrefsToRoom(context: Context, transactionDao: TransactionDao) {
    withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("myrupees_prefs", Context.MODE_PRIVATE)
        val jsonTransactions = prefs.getString("transactions", null)
        val jsonRecurring = prefs.getString("recurring_transactions", null)

        val gson = GsonBuilder()
            .registerTypeAdapter(Transaction::class.java, TransactionDeserializer())
            .registerTypeAdapter(RecurringTransaction::class.java, RecurringTransactionDeserializer())
            .create()

        jsonTransactions?.let {
            try {
                Log.d("Migration", "Deserializing transactions: $it")
                val transactions = gson.fromJson<List<Transaction>>(
                    it,
                    object : TypeToken<List<Transaction>>() {}.type
                )
                transactions.forEach { transaction ->
                    Log.d("Migration", "Inserting transaction: $transaction")
                    transactionDao.insertTransaction(transaction)
                }
            } catch (e: Exception) {
                Log.e("Migration", "Error deserializing transactions: ${e.message}", e)
            }
        }

        jsonRecurring?.let {
            try {
                Log.d("Migration", "Deserializing recurring transactions: $it")
                val recurring = gson.fromJson<List<RecurringTransaction>>(
                    it,
                    object : TypeToken<List<RecurringTransaction>>() {}.type
                )
                recurring.forEach { recurringTransaction ->
                    Log.d("Migration", "Inserting recurring transaction: $recurringTransaction")
                    transactionDao.insertRecurringTransaction(recurringTransaction)
                }
            } catch (e: Exception) {
                Log.e("Migration", "Error deserializing recurring transactions: ${e.message}", e)
            }
        }

        val users = prefs.all.keys.filter { it.startsWith("budget_") }.map { it.removePrefix("budget_") }
        users.forEach { user ->
            val budgetAmount = prefs.getFloat("budget_$user", 0f).toDouble()
            val month = prefs.getInt("saved_month_$user", Calendar.getInstance().get(Calendar.MONTH))
            transactionDao.insertBudget(Budget(user, budgetAmount, month))
        }

        prefs.all.forEach { (key, value) ->
            if (key.startsWith("feedback_")) {
                val username = key.removePrefix("feedback_")
                val feedback = value.toString()
                transactionDao.insertFeedback(Feedback(user = username, feedback = feedback, timestamp = System.currentTimeMillis()))
            }
        }

        prefs.edit().clear().apply()
    }
}