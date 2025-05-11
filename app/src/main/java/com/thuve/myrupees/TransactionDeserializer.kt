package com.thuve.myrupees

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class TransactionDeserializer : JsonDeserializer<Transaction> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Transaction {
        val jsonObject = json.asJsonObject

        // Map JSON fields to Transaction fields, handling mismatches
        val title = jsonObject.get("title").asString
        val amount = jsonObject.get("amount").asDouble
        val category = jsonObject.get("category").asString
        val date = jsonObject.get("date").asString
        val type = jsonObject.get("type").asString
        // Handle "AvaiBal" (old JSON key) to "avaiBal" (new field name)
        val avaiBal = if (jsonObject.has("AvaiBal")) {
            jsonObject.get("AvaiBal").asDouble
        } else {
            jsonObject.get("avaiBal")?.asDouble ?: 0.0
        }
        val user = jsonObject.get("user").asString

        // Ignore the "id" field since Room will auto-generate it
        return Transaction(
            id = 0, // Room will auto-generate the ID
            title = title,
            amount = amount,
            category = category,
            date = date,
            type = type,
            avaiBal = avaiBal,
            user = user
        )
    }
}