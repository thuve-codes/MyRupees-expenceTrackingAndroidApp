package com.thuve.myrupees
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class, RecurringTransaction::class, Budget::class, Feedback::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS budgets (user TEXT PRIMARY KEY NOT NULL, amount REAL NOT NULL, month INTEGER NOT NULL)")
            }
        }
    }
}