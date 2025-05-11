package com.thuve.myrupees

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecurringNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sharedPref = applicationContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("current_user", "Guest") ?: "Guest"
        val viewModel = TransactionViewModel(
            AppDatabase.getDatabase(applicationContext).transactionDao(),
            currentUser
        )
        val upcomingTransactions = viewModel.upcomingRecurringTransactions.first()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Get current date and calculate range for the next 3 days
        val calendar = Calendar.getInstance()
        val currentDate = sdf.format(calendar.time)
        val endDateCalendar = calendar.clone() as Calendar
        endDateCalendar.add(Calendar.DAY_OF_YEAR, 3)
        val endDate = sdf.format(endDateCalendar.time)

        Log.d("RecurringNotification", "Checking for upcoming transactions for user: $currentUser (from $currentDate to $endDate)")
        if (upcomingTransactions.isNotEmpty()) {
            upcomingTransactions.forEach { transaction ->
                Log.d("RecurringNotification", "Found transaction: ${transaction.title}, Date: ${transaction.scheduledDate}")
                val transactionDate = sdf.parse(transaction.scheduledDate)
                if (transactionDate != null) {
                    val transactionCalendar = Calendar.getInstance().apply { time = transactionDate }
                    if (!transactionCalendar.before(calendar) && !transactionCalendar.after(endDateCalendar)) {
                        NotificationHelper.showNotification(
                            applicationContext,
                            "Upcoming Recurring Transaction",
                            "Transaction '${transaction.title}' of Rs ${transaction.amount} is due within 3 days: ${transaction.scheduledDate}"
                        )
                        Log.d("RecurringNotification", "Notification sent for: ${transaction.title}")
                    }
                }
            }
        } else {
            Log.d("RecurringNotification", "No upcoming transactions found")
        }

        // Reschedule the worker to run again after 3 seconds
        val nextWorkRequest = OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "recurringNotificationWork",
            androidx.work.ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )
        Log.d("RecurringNotification", "Worker rescheduled to run in 3 seconds")

        return Result.success()
    }
}