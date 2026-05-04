package com.example.tusevinge

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvType.text = transaction.type
        holder.tvAmount.text = "UGX ${String.format("%,.0f", transaction.amount)}"
        holder.tvDate.text = transaction.date
        holder.tvStatus.text = transaction.status

        if (transaction.type.lowercase() == "deposit") {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")) // Green
            holder.tvAmount.text = "+ ${holder.tvAmount.text}"
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")) // Red
            holder.tvAmount.text = "- ${holder.tvAmount.text}"
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
