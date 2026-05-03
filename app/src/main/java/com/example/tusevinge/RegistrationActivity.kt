package com.example.tusevinge

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
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()

        btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRegister.setOnClickListener {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (validateInput(name, email, password)) {
                lifecycleScope.launch {
                    try {
                        val existingUser = userDao.getUserByName(name)
                        if (existingUser == null) {
                            
                            // Save image to internal storage if selected
                            val internalPath = if (selectedImageUri != null) {
                                saveImageToInternalStorage(selectedImageUri!!, name)
                            } else null

                            val newUser = User(
                                name = name,
                                email = email,
                                password = password,
                                profileImageUri = internalPath
                            )
                            userDao.registerUser(newUser)
                            Toast.makeText(this@RegistrationActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@RegistrationActivity, "User with this name already exists", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("Registration", "Database error", e)
                        Toast.makeText(this@RegistrationActivity, "Error accessing database. Please reinstall the app.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri, name: String): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val cleanName = name.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val file = File(filesDir, "profile_$cleanName.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
