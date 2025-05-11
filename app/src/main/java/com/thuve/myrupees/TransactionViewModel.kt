package com.thuve.myrupees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(private val dao: TransactionDao, private val currentUser: String) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions(currentUser)
    val upcomingRecurringTransactions: Flow<List<RecurringTransaction>> = dao.getUpcomingRecurringTransactions(currentUser)
    val recentRecurringTransactions: Flow<List<RecurringTransaction>> = dao.getRecentRecurringTransactions(currentUser)
    val allFeedbacks: Flow<List<Feedback>> = dao.getAllFeedbacks()

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch { dao.insertTransaction(transaction) }
    fun updateTransaction(transaction: Transaction) = viewModelScope.launch { dao.updateTransaction(transaction) }
    fun insertRecurringTransaction(recurringTransaction: RecurringTransaction) = viewModelScope.launch { dao.insertRecurringTransaction(recurringTransaction) }
    fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) = viewModelScope.launch { dao.updateRecurringTransaction(recurringTransaction) }
    suspend fun getBudget() = dao.getBudget(currentUser)
    fun insertBudget(budget: Budget) = viewModelScope.launch { dao.insertBudget(budget) }
    fun insertFeedback(feedback: Feedback) = viewModelScope.launch { dao.insertFeedback(feedback) }
    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        dao.deleteTransaction(transaction)
    }

}