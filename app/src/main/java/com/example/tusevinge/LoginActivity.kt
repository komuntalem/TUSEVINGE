package com.example.tusevinge

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private var isLoggingIn = false
    private lateinit var auth: FirebaseAuth
    // Updated to your new Database URL
    private val DB_URL = "https://savingsapp-e1241-default-rtdb.firebaseio.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val edtName = findViewById<EditText>(R.id.edtLoginName)
        val edtPassword = findViewById<EditText>(R.id.edtLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        btnLogin.setOnClickListener {
            val nameOrEmail = edtName.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (nameOrEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                isLoggingIn = true
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(nameOrEmail).matches()) {
                    // If user entered an email, sign in directly
                    loginWithAuth(nameOrEmail, password, null)
                } else {
                    // If user entered a name, find their email first
                    findEmailAndLogin(nameOrEmail, password)
                }
                
                // Connection Timeout Fallback
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isLoggingIn && !isFinishing) {
                        Toast.makeText(applicationContext, "Connection timeout. Check your internet.", Toast.LENGTH_LONG).show()
                        isLoggingIn = false
                    }
                }, 15000)
            }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun findEmailAndLogin(name: String, password: String) {
        val sanitizedName = name.replace(Regex("[.#$\\[\\]]"), "_")
        val dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("users")

        dbRef.child(sanitizedName).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val email = snapshot.child("email").value?.toString()
                if (email != null) {
                    loginWithAuth(email, password, name)
                } else {
                    isLoggingIn = false
                    Toast.makeText(this, "Email not found for this user", Toast.LENGTH_SHORT).show()
                }
            } else {
                isLoggingIn = false
                Toast.makeText(this, "User not found in database. Please register.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            isLoggingIn = false
            Toast.makeText(this, "Database Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginWithAuth(email: String, password: String, originalName: String?) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                isLoggingIn = false
                // Use the name from the database or the prefix of the email
                val displayName = originalName ?: email.substringBefore("@")
                
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USERNAME", displayName)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                isLoggingIn = false
                Log.e("LoginError", e.message ?: "Unknown error")
                Toast.makeText(this, "Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
