package com.thuve.myrupees

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecurringNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RecurringWorker", "Worker started")
        val dao = DatabaseProvider.getDatabase(applicationContext).transactionDao()
        val user = SharedPrefManager.getCurrentUsername(applicationContext)
        val recurringTransactions = dao.getAllRecurringTransactions(user).first()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val upcomingList = recurringTransactions.filter { !it.paid }.filter { transaction ->
            val transactionDate = dateFormat.parse(transaction.scheduledDate)
            transactionDate?.let { date ->
                val diff = date.time - today.timeInMillis
                diff in 0..(10 * DateUtils.DAY_IN_MILLIS)
            } ?: false
        }

        if (upcomingList.isNotEmpty()) {
            NotificationHelper.showNotification(
                applicationContext,
                "Upcoming Payment for $user",
                "You have ${upcomingList.size} unpaid recurring transaction(s) due within 10 days."
            )
        }

        scheduleNextRun()
        return Result.success()
    }

    private fun scheduleNextRun() {
        val request = androidx.work.OneTimeWorkRequestBuilder<RecurringNotificationWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
        Log.d("RecurringWorker", "Scheduled next run in 3 seconds")
    }
}