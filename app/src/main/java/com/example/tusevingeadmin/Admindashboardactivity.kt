package com.example.tusevingeadmin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tusevingeadmin.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, AdminLoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}