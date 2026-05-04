package com.example.tusevingeadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminLoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

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
            _loginState.value = LoginState.Error("Please fill in all fields")
            return
        }
        _loginState.value = LoginState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { _loginState.value = LoginState.Success }
            .addOnFailureListener { e -> _loginState.value = LoginState.Error(e.message ?: "Login failed") }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}