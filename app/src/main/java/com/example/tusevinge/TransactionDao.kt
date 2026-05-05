package com.example.tusevinge

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT TOTAL(CASE WHEN type = 'deposit' THEN amount ELSE -amount END) FROM transactions")
    suspend fun getBalance(): Double
}
