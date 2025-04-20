package com.thuve.myrupees

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String ,// "Income" or "Expense"
    val AvaiBal :Double
)
