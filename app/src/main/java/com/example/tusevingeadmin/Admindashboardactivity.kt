package com.example.tusevingeadmin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tusevingeadmin.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        observeViewModel()

        binding.cardAllMembers.setOnClickListener {
            startActivity(Intent(this, AllUsersActivity::class.java))
        }
        binding.cardAllTransactions.setOnClickListener {
            startActivity(Intent(this, AllTransactionsActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.totalUsers.observe(this) { binding.tvTotalUsers.text = it.toString() }
        viewModel.totalSaved.observe(this) { binding.tvTotalSaved.text = "UGX ${"%,d".format(it)}" }
        viewModel.todayTxnCount.observe(this) { binding.tvTodayTxns.text = it.toString() }
    }
}