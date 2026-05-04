package com.example.tusevingeadmin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tusevingeadmin.databinding.ActivityAllUsersBinding

class AllUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllUsersBinding
    private val viewModel: AllUsersViewModel by viewModels()
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupRecyclerView()
        observeViewModel()
        setupSearch()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        adapter = UsersAdapter { user ->
            val intent = Intent(this, UserDetailActivity::class.java).apply {
                putExtra(UserDetailActivity.EXTRA_USER_ID, user.id)
            }
            startActivity(intent)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.filteredUsers.observe(this) { adapter.submitList(it) }
        viewModel.totalCount.observe(this) { supportActionBar?.subtitle = "$it registered" }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.setSearchQuery(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
