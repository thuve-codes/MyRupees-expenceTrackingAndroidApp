package com.thuve.myrupees
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao, private val user: String) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions(user)
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = transactionDao.getAllRecurringTransactions(user)
    val upcomingRecurringTransactions: Flow<List<RecurringTransaction>> = transactionDao.getUpcomingRecurringTransactions(user)
    val recentRecurringTransactions: Flow<List<RecurringTransaction>> = transactionDao.getRecentRecurringTransactions(user)
    val allFeedbacks: Flow<List<Feedback>> = transactionDao.getAllFeedbacks()

    suspend fun getBudget(): Budget? = transactionDao.getBudget(user)

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction) {
        transactionDao.insertRecurringTransaction(recurringTransaction)
    }

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        transactionDao.updateRecurringTransaction(recurringTransaction)
    }

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        transactionDao.deleteRecurringTransaction(recurringTransaction)
    }

    suspend fun insertBudget(budget: Budget) {
        transactionDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        transactionDao.updateBudget(budget)
    }

    suspend fun insertFeedback(feedback: Feedback) {
        transactionDao.insertFeedback(feedback)
    }
}