package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun getAllTransactions(): LiveData<List<Transaction>> {
        val liveData = MutableLiveData<List<Transaction>>()
        db.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                liveData.value = snapshot.documents.map { toTransaction(it) }
            }
        return liveData
    }

    fun getTransactionsByUser(userId: String): LiveData<List<Transaction>> {
        val liveData = MutableLiveData<List<Transaction>>()
        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                liveData.value = snapshot.documents.map { toTransaction(it) }
            }
        return liveData
    }

    fun getTodayTransactions(): LiveData<List<Transaction>> {
        val liveData = MutableLiveData<List<Transaction>>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        db.collection("transactions")
            .whereGreaterThan("timestamp", todayStart)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                liveData.value = snapshot.documents.map { toTransaction(it) }
            }
        return liveData
    }

    private fun toTransaction(doc: com.google.firebase.firestore.DocumentSnapshot): Transaction {
        val ts = doc.getLong("timestamp") ?: 0L
        val date = Date(ts)
        return Transaction(
            id = doc.id,
            userId = doc.getString("userId") ?: "",
            userName = doc.getString("userName") ?: "",
            type = doc.getString("type") ?: "deposit",
            method = doc.getString("method") ?: "MTN",
            amount = doc.getLong("amount") ?: 0L,
            balanceAfter = doc.getLong("balanceAfter") ?: 0L,
            timestamp = ts,
            dateLabel = dateFmt.format(date),
            timeLabel = timeFmt.format(date)
        )
    }
}