package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap

class AllTransactionsViewModel : ViewModel() {

    private val txnRepo = TransactionRepository()
    private val allTransactions = txnRepo.getAllTransactions()

    private val _filter = MutableLiveData("all")
    private val _searchQuery = MutableLiveData("")

    val filteredTransactions: LiveData<List<Transaction>> = _filter.switchMap { filter ->
        _searchQuery.switchMap { query ->
            allTransactions.switchMap { list ->
                val result = MutableLiveData<List<Transaction>>()
                var filtered = list
                if (filter != "all") filtered = filtered.filter { it.type == filter }
                if (query.isNotBlank()) {
                    val q = query.lowercase()
                    filtered = filtered.filter {
                        it.userName.lowercase().contains(q) || it.dateLabel.lowercase().contains(q)
                    }
                }
                result.value = filtered
                result
            }
        }
    }

    fun setFilter(filter: String) { _filter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
}