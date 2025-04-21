package com.thuve.myrupees

data class RecurringTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val scheduledDate: String,
    val isPaid: Boolean = false
)
