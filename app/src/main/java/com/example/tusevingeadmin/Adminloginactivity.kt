package com.example.tusevingeadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tusevingeadmin.databinding.ActivityAdminLoginBinding

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLoginBinding
    private val viewModel: AdminLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isAlreadyLoggedIn) {
            goToDashboard()
            return
        }

        observeViewModel()

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etAdminEmail.text.toString().trim(),
                binding.etAdminPassword.text.toString().trim()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AdminLoginViewModel.LoginState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "LOG IN"
                    binding.progressBar.visibility = View.GONE
                }
                is AdminLoginViewModel.LoginState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Signing in..."
                    binding.progressBar.visibility = View.VISIBLE
                }
                is AdminLoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    goToDashboard()
                }
                is AdminLoginViewModel.LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "LOG IN"
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }
}