package com.example.tusevingeadmin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminLoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AdminLoginViewModel"

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    val isAlreadyLoggedIn: Boolean
        get() = auth.currentUser != null

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please enter email and password")
            return
        }

        _loginState.value = LoginState.Loading
        Log.d(TAG, "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user?.email == "admin@tusevinge.com") {
                        Log.d(TAG, "Login Success - Admin")
                        _loginState.value = LoginState.Success
                    } else {
                        Log.w(TAG, "Login Failed - Not Admin")
                        auth.signOut()
                        _loginState.value = LoginState.Error("Access denied. Not an admin account.")
                    }
                } else {
                    Log.w(TAG, "Login Failed: ${task.exception?.message}")
                    _loginState.value = LoginState.Error("Login failed: ${task.exception?.message}")
                }
            }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
