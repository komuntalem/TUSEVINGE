package com.example.tusevinge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val status: String = "Completed",
    val phone: String = ""
)
