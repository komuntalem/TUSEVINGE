package com.example.tusevingeadmin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository {

    private val db = FirebaseDatabase.getInstance().getReference("transactions")
    private val TAG = "TransactionRepository"
    private val dateFmt = SimpleDateFormat("MMM d", Locale.US)
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.US)

    fun getAllTransactions(): LiveData<List<Transaction>> {
        val liveData = MutableLiveData<List<Transaction>>()
        
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (txnSnapshot in snapshot.children) {
                    val txn = toTransaction(txnSnapshot)
                    if (txn != null) list.add(txn)
                }
                // Sort by timestamp descending
                list.sortByDescending { it.timestamp }
                Log.d(TAG, "Fetched ${list.size} transactions from RTDB")
                liveData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching transactions: ${error.message}")
            }
        })
        return liveData
    }

    fun getTransactionsByUser(userId: String): LiveData<List<Transaction>> {
        val liveData = MutableLiveData<List<Transaction>>()
        // Since flat structure, fetch all and filter
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (txnSnapshot in snapshot.children) {
                    val txn = toTransaction(txnSnapshot)
                    if (txn != null && txn.userId == userId) list.add(txn)
                }
                list.sortByDescending { it.timestamp }
                liveData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching user transactions: ${error.message}")
            }
        })
        return liveData
    }

    fun getTodayTransactions(): LiveData<List<Transaction>> {
        val liveData = MutableLiveData<List<Transaction>>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis
        
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (txnSnapshot in snapshot.children) {
                    val txn = toTransaction(txnSnapshot)
                    if (txn != null && txn.timestamp >= todayStart) list.add(txn)
                }
                list.sortByDescending { it.timestamp }
                liveData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching today's transactions: ${error.message}")
            }
        })
        return liveData
    }

    private fun toTransaction(snapshot: DataSnapshot, userId: String? = null): Transaction? {
        return try {
            val ts = snapshot.child("timestamp").getValue(Long::class.java) 
                ?: snapshot.child("date").getValue(Long::class.java) 
                ?: 0L
            
            val username = snapshot.child("username").getValue(String::class.java)
                ?: snapshot.child("userName").getValue(String::class.java)
                ?: snapshot.child("name").getValue(String::class.java)
                ?: "Unknown User"

            val date = Date(ts)
            Transaction(
                id = snapshot.key ?: "",
                userId = userId ?: username,
                userName = username,
                type = snapshot.child("type").getValue(String::class.java)?.lowercase() ?: "deposit",
                method = snapshot.child("method").getValue(String::class.java) ?: "Mobile Money",
                amount = snapshot.child("amount").getValue(Long::class.java) ?: 0L,
                balanceAfter = snapshot.child("balanceAfter").getValue(Long::class.java) ?: 0L,
                timestamp = ts,
                dateLabel = dateFmt.format(date),
                timeLabel = timeFmt.format(date)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing transaction ${snapshot.key}: ${e.message}")
            null
        }
    }
}
