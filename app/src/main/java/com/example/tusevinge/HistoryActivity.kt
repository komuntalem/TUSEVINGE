package com.example.tusevinge

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val username = intent.getStringExtra("USERNAME") ?: "User"

        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(transactionList)
        recyclerView.adapter = adapter

        fetchTransactionHistory(username)
    }

    private fun fetchTransactionHistory(username: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("transactions").child(username)
        
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                for (child in snapshot.children) {
                    val id = child.child("id").value?.toString() ?: ""
                    val type = child.child("type").value?.toString() ?: ""
                    val amount = child.child("amount").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val date = child.child("date").value?.toString() ?: ""
                    
                    transactionList.add(Transaction(type = type, amount = amount, date = date))
                }
                // Reverse to show latest first
                transactionList.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
