package com.thuve.myrupees

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class RecurringTransactionDeserializer : JsonDeserializer<RecurringTransaction> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): RecurringTransaction {
        val jsonObject = json.asJsonObject

        val title = jsonObject.get("title").asString
        val amount = jsonObject.get("amount").asDouble
        val scheduledDate = jsonObject.get("scheduledDate").asString
        val paid = jsonObject.get("paid").asBoolean
        val user = jsonObject.get("user").asString

        // Ignore the "id" field since Room will auto-generate it
        return RecurringTransaction(
            id = 0, // Room will auto-generate the ID
            title = title,
            amount = amount,
            scheduledDate = scheduledDate,
            paid = paid,
            user = user
        )
    }
}