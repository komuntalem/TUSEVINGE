package com.example.tusevinge

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) 
    val localId: Int = 0,
    val id: String = "",
    val username: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val timestamp: Long = 0L,
    val status: String = "Completed",
    val phone: String = ""
) {
    // Required empty constructor for Firebase
    constructor() : this(0, "", "", "", 0.0, "", 0L, "Completed", "")
}
