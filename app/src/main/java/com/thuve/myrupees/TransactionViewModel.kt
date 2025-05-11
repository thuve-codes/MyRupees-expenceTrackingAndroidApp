package com.thuve.myrupees
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = repository.allTransactions
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = repository.allRecurringTransactions
    val upcomingRecurringTransactions: Flow<List<RecurringTransaction>> = repository.upcomingRecurringTransactions
    val recentRecurringTransactions: Flow<List<RecurringTransaction>> = repository.recentRecurringTransactions
    val allFeedbacks: Flow<List<Feedback>> = repository.allFeedbacks

    suspend fun getBudget(): Budget? = repository.getBudget()

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun insertRecurringTransaction(recurringTransaction: RecurringTransaction) = viewModelScope.launch {
        repository.insertRecurringTransaction(recurringTransaction)
    }

    fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) = viewModelScope.launch {
        repository.updateRecurringTransaction(recurringTransaction)
    }

    fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) = viewModelScope.launch {
        repository.deleteRecurringTransaction(recurringTransaction)
    }

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun updateBudget(budget: Budget) = viewModelScope.launch {
        repository.updateBudget(budget)
    }

    fun insertFeedback(feedback: Feedback) = viewModelScope.launch {
        repository.insertFeedback(feedback)
    }
}

class TransactionViewModelFactory(private val context: Context, private val user: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(TransactionRepository(DatabaseProvider.getDatabase(context).transactionDao(), user)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}