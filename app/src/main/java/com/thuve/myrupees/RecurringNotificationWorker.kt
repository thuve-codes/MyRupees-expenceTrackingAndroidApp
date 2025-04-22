package com.thuve.myrupees

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecurringNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("RecurringWorker", "Worker started")

        val recurringTransactions = SharedPrefManager.loadRecurringTransactions(applicationContext)
        Log.d("RecurringWorker", "Loaded ${recurringTransactions.size} recurring transactions")

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val upcomingList = recurringTransactions.filter { transaction ->
            try {
                if (transaction.paid) return@filter false

                val transactionDate = dateFormat.parse(transaction.scheduledDate)
                if (transactionDate != null) {
                    val diff = transactionDate.time - today.timeInMillis
                    Log.d("RecurringWorker", "Transaction '${transaction.title}' is in ${diff / DateUtils.DAY_IN_MILLIS} day(s)")
                    diff in 0..(10 * DateUtils.DAY_IN_MILLIS)
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        if (upcomingList.isNotEmpty()) {
            NotificationHelper.showNotification(
                applicationContext,
                "Upcoming Payment",
                "You have ${upcomingList.size} unpaid recurring transaction(s) due within days."
            )
        }

        scheduleNextRun()
        return Result.success()
    }

    private fun scheduleNextRun() {
        val request = OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
        Log.d("RecurringWorker", "Scheduled next run in 10 seconds")
    }
}
