package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class UserDetailViewModel(private val userId: String) : ViewModel() {

    private val userRepo = UserRepository()
    private val txnRepo = TransactionRepository()

    val user: LiveData<User?> = userRepo.getUserById(userId)
    val transactions: LiveData<List<Transaction>> = txnRepo.getTransactionsByUser(userId)

    val totalSaved: LiveData<Long> = transactions.map { list ->
        list.filter { it.isDeposit }.sumOf { it.amount }
    }
    val totalWithdrawn: LiveData<Long> = transactions.map { list ->
        list.filter { !it.isDeposit }.sumOf { it.amount }
    }
}