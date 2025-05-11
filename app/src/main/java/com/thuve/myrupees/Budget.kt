package com.thuve.myrupees
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val user: String,
    val amount: Double,
    val month: Int // Store the month (0-11) for monthly reset
)