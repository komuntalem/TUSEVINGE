package com.example.tusevingeadmin

data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val balance: Long = 0L,
    val joinedDate: String = "",
    val role: String = "user"
) {
    fun initials(): String {
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "??"
        }
    }
}