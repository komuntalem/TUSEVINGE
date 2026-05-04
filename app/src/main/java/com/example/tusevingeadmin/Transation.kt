package com.example.tusevingeadmin

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val type: String = "deposit",
    val method: String = "MTN",
    val amount: Long = 0L,
    val balanceAfter: Long = 0L,
    val timestamp: Long = 0L,
    val dateLabel: String = "",
    val timeLabel: String = ""
) {
    val isDeposit get() = type == "deposit"
    val formattedAmount get() = if (isDeposit) "+${"%,d".format(amount)}" else "-${"%,d".format(amount)}"
    val amountColor get() = if (isDeposit) "#2E7D32" else "#C62828"
}