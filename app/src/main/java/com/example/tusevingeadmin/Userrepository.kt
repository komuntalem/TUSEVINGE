package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getUsers(): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        db.collection("users")
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                liveData.value = snapshot.documents.map { doc ->
                    User(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        email = doc.getString("email") ?: "",
                        balance = doc.getLong("balance") ?: 0L,
                        joinedDate = doc.getString("joinedDate") ?: "",
                        role = doc.getString("role") ?: "user"
                    )
                }
            }
        return liveData
    }

    fun getUserById(userId: String): LiveData<User?> {
        val liveData = MutableLiveData<User?>()
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                liveData.value = User(
                    id = snapshot.id,
                    name = snapshot.getString("name") ?: "",
                    phone = snapshot.getString("phone") ?: "",
                    email = snapshot.getString("email") ?: "",
                    balance = snapshot.getLong("balance") ?: 0L,
                    joinedDate = snapshot.getString("joinedDate") ?: "",
                    role = snapshot.getString("role") ?: "user"
                )
            }
        return liveData
    }
}