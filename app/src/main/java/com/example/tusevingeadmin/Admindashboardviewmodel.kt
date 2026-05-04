package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AdminDashboardViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val txnRepo = TransactionRepository()

    private val users = userRepo.getUsers()
    private val todayTransactions = txnRepo.getTodayTransactions()

    val totalUsers: LiveData<Int> = users.map { it.size }
    val totalSaved: LiveData<Long> = users.map { list -> list.sumOf { it.balance } }
    val todayTxnCount: LiveData<Int> = todayTransactions.map { it.size }
}