package com.thuve.myrupees
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedbacks")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user: String,
    val feedback: String,
    val timestamp: Long // For ordering feedback by time
)