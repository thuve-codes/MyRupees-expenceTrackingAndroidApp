package com.thuve.myrupees

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TransactionViewModelFactory(private val context: Context, private val currentUser: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
        TransactionViewModel(AppDatabase.getDatabase(context).transactionDao(), currentUser) as T
    } else {
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}