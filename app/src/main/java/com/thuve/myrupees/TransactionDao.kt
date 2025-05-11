package com.thuve.myrupees
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE user = :user ORDER BY date DESC")
    fun getAllTransactions(user: String): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM recurring_transactions WHERE user = :user ORDER BY scheduledDate DESC")
    fun getAllRecurringTransactions(user: String): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE user = :user AND paid = 0")
    fun getUpcomingRecurringTransactions(user: String): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE user = :user AND paid = 1")
    fun getRecentRecurringTransactions(user: String): Flow<List<RecurringTransaction>>

    @Insert
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Query("SELECT * FROM budgets WHERE user = :user")
    suspend fun getBudget(user: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<Feedback>>

    @Insert
    suspend fun insertFeedback(feedback: Feedback)

}