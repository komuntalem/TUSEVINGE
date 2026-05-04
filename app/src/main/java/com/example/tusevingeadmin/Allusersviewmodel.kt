package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap

class AllUsersViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val allUsers = userRepo.getUsers()
    private val _searchQuery = MutableLiveData("")

    val filteredUsers: LiveData<List<User>> = _searchQuery.switchMap { query ->
        allUsers.switchMap { users ->
            val result = MutableLiveData<List<User>>()
            result.value = if (query.isBlank()) users
            else {
                val q = query.lowercase()
                users.filter { it.name.lowercase().contains(q) || it.phone.contains(q) }
            }
            result
        }
    }

    val totalCount: LiveData<Int> = allUsers.switchMap { users ->
        val result = MutableLiveData<Int>()
        result.value = users.size
        result
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}