package com.example.tusevingeadmin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val db = FirebaseDatabase.getInstance().getReference("users")
    private val TAG = "UserRepository"

    // Fetches all users and updates automatically when data changes
    fun getUsers(): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = parseUser(userSnapshot)
                    if (user != null) usersList.add(user)
                }
                Log.d(TAG, "Fetched ${usersList.size} users from Realtime Database")
                liveData.value = usersList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
        return liveData
    }

    // Handles different field naming conventions from the User app
    private fun parseUser(snapshot: DataSnapshot): User? {
        return try {
            User(
                id = snapshot.key ?: "",
                // Try all common naming variations for Name
                name = snapshot.child("name").getValue(String::class.java)
                    ?: snapshot.child("fullName").getValue(String::class.java)
                    ?: snapshot.child("userName").getValue(String::class.java)
                    ?: snapshot.child("username").getValue(String::class.java)
                    ?: snapshot.key
                    ?: "Unknown User",
                // Try all common naming variations for Phone
                phone = snapshot.child("phone").getValue(String::class.java)
                    ?: snapshot.child("phoneNumber").getValue(String::class.java)
                    ?: "No Phone",
                email = snapshot.child("email").getValue(String::class.java) ?: "",
                // Handles Long vs Double for balance safely
                balance = snapshot.child("balance").getValue(Long::class.java) ?: 0L,
                // Try different date field names
                joinedDate = snapshot.child("joinedDate").getValue(String::class.java)
                    ?: snapshot.child("createdAt").getValue(String::class.java)
                    ?: "N/A",
                role = snapshot.child("role").getValue(String::class.java) ?: "user"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing user ${snapshot.key}: ${e.message}")
            null
        }
    }

    fun getUserById(userId: String): LiveData<User?> {
        val liveData = MutableLiveData<User?>()
        db.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                liveData.value = if (snapshot.exists()) parseUser(snapshot) else null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching user $userId: ${error.message}")
            }
        })
        return liveData
    }
}