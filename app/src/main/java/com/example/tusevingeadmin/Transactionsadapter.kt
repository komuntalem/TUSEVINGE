package com.example.tusevingeadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tusevingeadmin.databinding.ItemTransactionBinding

class TransactionsAdapter : ListAdapter<Transaction, TransactionsAdapter.TxnViewHolder>(DiffCallback()) {

    inner class TxnViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(txn: Transaction) {
            binding.tvTypeIcon.text = if (txn.isDeposit) "⬇" else "⬆"
            binding.tvTypeIcon.setBackgroundResource(
                if (txn.isDeposit) R.drawable.bg_deposit_circle else R.drawable.bg_withdraw_circle
            )
            binding.tvUserName.text = txn.userName
            binding.tvDescription.text =
                "${txn.type.replaceFirstChar { it.uppercase() }} · ${txn.method} · ${txn.dateLabel}"
            binding.tvAmount.text = txn.formattedAmount
            binding.tvAmount.setTextColor(Color.parseColor(txn.amountColor))
            binding.tvTime.text = txn.timeLabel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxnViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TxnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TxnViewHolder, position: Int) { holder.bind(getItem(position)) }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}