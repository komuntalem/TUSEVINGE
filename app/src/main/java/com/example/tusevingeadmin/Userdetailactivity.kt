package com.example.tusevingeadmin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tusevingeadmin.databinding.ActivityUserDetailBinding

class UserDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    private lateinit var binding: ActivityUserDetailBinding
    private lateinit var viewModel: UserDetailViewModel
    private lateinit var adapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: run { finish(); return }

        val factory = UserDetailViewModelFactory(userId)
        viewModel = ViewModelProvider(this, factory)[UserDetailViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TransactionsAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
        binding.rvTransactions.isNestedScrollingEnabled = false
    }

    private fun observeViewModel() {
        viewModel.user.observe(this) { user ->
            user ?: return@observe
            binding.tvAvatar.text = user.initials()
            binding.tvName.text = user.name
            binding.tvPhone.text = user.phone
            binding.tvJoined.text = "Joined: ${user.joinedDate}"
            binding.tvBalance.text = "%,d".format(user.balance)
        }
        viewModel.totalSaved.observe(this) { binding.tvTotalSaved.text = "%,d".format(it) }
        viewModel.totalWithdrawn.observe(this) { binding.tvTotalWithdrawn.text = "%,d".format(it) }
        viewModel.transactions.observe(this) { adapter.submitList(it) }
    }
}