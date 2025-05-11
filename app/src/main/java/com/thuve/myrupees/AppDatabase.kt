package com.thuve.myrupees

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class, RecurringTransaction::class, Budget::class, Feedback::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myrupees_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            Log.d("AppDatabase", "Database created with initial schema")
                            db.execSQL("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, amount REAL NOT NULL, category TEXT NOT NULL, date TEXT NOT NULL, type TEXT NOT NULL, avaiBal REAL NOT NULL, user TEXT NOT NULL)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS recurring_transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, amount REAL NOT NULL, scheduledDate TEXT NOT NULL, paid INTEGER NOT NULL, user TEXT NOT NULL)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS budgets (user TEXT PRIMARY KEY NOT NULL, amount REAL NOT NULL, month INTEGER NOT NULL)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS feedbacks (user TEXT NOT NULL, feedback TEXT NOT NULL, timestamp INTEGER NOT NULL)")
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            Log.d("AppDatabase", "Database opened")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}