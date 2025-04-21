package com.thuve.myrupees

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonHelper {
    private val gson = Gson()

    fun fromJsonToRecurringTransactionList(json: String): List<RecurringTransaction> {
        val type = object : TypeToken<List<RecurringTransaction>>() {}.type
        return gson.fromJson(json, type)
    }
}
