package com.tusevinge.admin.utils

object FormatUtils {
    fun formatAmount(amount: Long): String = "%,d".format(amount)
    fun formatUgx(amount: Long): String = "UGX ${formatAmount(amount)}"
}