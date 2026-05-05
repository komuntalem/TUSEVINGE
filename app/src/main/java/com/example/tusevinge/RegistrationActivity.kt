package com.example.tusevinge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var imgProfilePreview: ImageView
    private lateinit var auth: FirebaseAuth
    private val DB_URL = "https://savingsapp-e1241-default-rtdb.firebaseio.com"

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imgProfilePreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()

        val edtName = findViewById<EditText>(R.id.edtRegisterName)
        val edtEmail = findViewById<EditText>(R.id.edtRegisterEmail)
        val edtPassword = findViewById<EditText>(R.id.edtRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        val btnSelectPhoto = findViewById<Button>(R.id.btnSelectPhoto)
        imgProfilePreview = findViewById(R.id.imgProfilePreview)

        btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRegister.setOnClickListener {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (validateInput(name, email, password)) {
                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show()
                createAccountWithAuth(name, email, password)
            }
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun createAccountWithAuth(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    if (selectedImageUri != null) {
                        uploadImageAndSaveData(userId, name, email)
                    } else {
                        saveUserDataToDatabase(userId, name, email, null)
                    }
                } else {
                    val error = task.exception?.message ?: "Unknown Auth Error"
                    Toast.makeText(this, "Auth Failed: $error", Toast.LENGTH_LONG).show()
                    Log.e("Registration", "Auth Error: $error")
                }
            }
    }

    private fun uploadImageAndSaveData(userId: String, name: String, email: String) {
        val storageRef = FirebaseStorage.getInstance().getReference("profile_images/$userId.jpg")
        storageRef.putFile(selectedImageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveUserDataToDatabase(userId, name, email, uri.toString())
            }
        }.addOnFailureListener {
            saveUserDataToDatabase(userId, name, email, null)
        }
    }

    private fun saveUserDataToDatabase(userId: String, name: String, email: String, imageUri: String?) {
        // Sanitize name: remove spaces and special characters for Firebase keys
        val sanitizedName = name.replace(Regex("[.#$\\[\\]\\s]"), "_")
        val dbRef = FirebaseDatabase.getInstance(DB_URL).getReference("users")
        
        val user = mapOf(
            "name" to name,
            "username" to name,
            "email" to email,
            "profileImageUri" to imageUri,
            "balance" to 0.0,
            "uid" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        dbRef.child(sanitizedName).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USERNAME", name)
                startActivity(intent)
                finish()
            } else {
                val error = task.exception?.message ?: "Unknown Database Error"
                Log.e("Registration", "DB Error: $error")
                Toast.makeText(this, "Database Error: $error. Check Rules!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Complete all fields correctly", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
