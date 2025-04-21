package com.thuve.myrupees

data class RecurringTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val scheduledDate: String,
    var paid: Boolean = false  // ✅ Add this line if it's missing
)

