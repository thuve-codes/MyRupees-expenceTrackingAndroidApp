package com.thuve.myrupees

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log

@Database(entities = [Transaction::class, RecurringTransaction::class, Budget::class, Feedback::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Migrating from version 1 to 2")
                database.execSQL("CREATE TABLE IF NOT EXISTS budgets (user TEXT PRIMARY KEY NOT NULL, amount REAL NOT NULL, month INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myrupees_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            Log.d("AppDatabase", "Database created")
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