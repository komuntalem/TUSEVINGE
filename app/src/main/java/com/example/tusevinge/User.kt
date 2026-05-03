package com.example.tusevinge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val name: String,
    val email: String,
    val password: String,
    val profileImageUri: String? = null
)
