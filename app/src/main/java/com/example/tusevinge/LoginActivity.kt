package com.example.tusevinge

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edtName = findViewById<EditText>(R.id.edtLoginName)
        val edtPassword = findViewById<EditText>(R.id.edtLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()

        btnLogin.setOnClickListener {
            val name = edtName.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (validateInput(name, password)) {
                lifecycleScope.launch {
                    val user = userDao.loginUser(name, password)
                    if (user != null) {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.putExtra("USERNAME", user.name)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid name or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun validateInput(name: String, password: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
