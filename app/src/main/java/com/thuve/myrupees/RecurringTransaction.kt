package com.thuve.myrupees
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val scheduledDate: String,
    var paid: Boolean,
    val user: String
)