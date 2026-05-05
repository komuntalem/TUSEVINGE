package com.example.tusevingeadmin

data class User(
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val balance: Long = 0L,
    val joinedDate: String = "",
    val role: String = "user"
) {
    fun initials(): String {
        return if (name.isNotEmpty()) name.take(2).uppercase() else "??"
    }
}