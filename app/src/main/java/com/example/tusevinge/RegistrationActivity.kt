package com.example.tusevinge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var imgProfilePreview: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imgProfilePreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

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
                Toast.makeText(this, "Registering user...", Toast.LENGTH_SHORT).show()
                registerUserWithFirebase(name, email, password)
            }
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun registerUserWithFirebase(name: String, email: String, password: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        
        dbRef.child(name).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
            } else {
                if (selectedImageUri != null) {
                    Toast.makeText(this, "Uploading profile photo...", Toast.LENGTH_SHORT).show()
                    uploadImageAndRegister(name, email, password)
                } else {
                    saveUserData(name, email, password, null)
                }
            }
        }.addOnFailureListener {
            Log.e("Registration", "Firebase Error: ${it.message}")
            Toast.makeText(this, "Connection error: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImageAndRegister(name: String, email: String, password: String) {
        val storageRef = FirebaseStorage.getInstance().getReference("profile_images/${UUID.randomUUID()}.jpg")
        
        storageRef.putFile(selectedImageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveUserData(name, email, password, uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            saveUserData(name, email, password, null) // Register anyway without photo
        }
    }

    private fun saveUserData(name: String, email: String, password: String, imageUri: String?) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        val user = mapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "profileImageUri" to imageUri
        )

        dbRef.child(name).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USERNAME", name)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save data: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Valid email required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
