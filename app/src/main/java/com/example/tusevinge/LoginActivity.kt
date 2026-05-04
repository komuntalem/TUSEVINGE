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
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edtName = findViewById<EditText>(R.id.edtLoginName)
        val edtPassword = findViewById<EditText>(R.id.edtLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        btnLogin.setOnClickListener {
            val name = edtName.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Connecting to server...", Toast.LENGTH_SHORT).show()
                loginUserWithFirebase(name, password)
                
                // Connection Timeout Fallback
                Handler(Looper.getMainLooper()).postDelayed({
                    // If we are still on this activity after 10 seconds, show a help message
                    Toast.makeText(applicationContext, "Connection is taking a long time. Check your internet and Firebase setup.", Toast.LENGTH_LONG).show()
                }, 10000)
            }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun loginUserWithFirebase(name: String, password: String) {
        // Explicitly getting reference. Ensure your database region matches!
        val dbRef = FirebaseDatabase.getInstance().getReference("users")

        dbRef.child(name).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val dbPassword = snapshot.child("password").value?.toString()
                if (dbPassword == password) {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("USERNAME", name)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User not found. Please register first.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", e.message ?: "Unknown error")
            Toast.makeText(this, "Login Failed: ${e.message}. Ensure database is enabled!", Toast.LENGTH_LONG).show()
        }
    }
}
