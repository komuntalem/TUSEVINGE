package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest

class AdminLoginViewModel : ViewModel() {

    // Password is "Tusevingenow" stored as SHA-256 hash
    private val ADMIN_PASSWORD_HASH = "4ca821368b2fc287dd01e8c3f5746b3760ab387706bc23bc2800da7885c0807e"

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    val isAlreadyLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please fill in all fields")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = LoginState.Error("Please enter a valid email")
            return
        }

        _loginState.value = LoginState.Loading

        if (hash(password) == ADMIN_PASSWORD_HASH) {
            _loginState.value = LoginState.Success
        } else {
            _loginState.value = LoginState.Error("Incorrect password")
        }
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
