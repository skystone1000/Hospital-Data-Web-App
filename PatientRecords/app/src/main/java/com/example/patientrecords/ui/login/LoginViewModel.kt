package com.example.patientrecords.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val loginResult = MutableLiveData<Boolean>()

    fun onLoginClicked() {
        val userEmail = email.value ?: ""
        val userPassword = password.value ?: ""

        loginResult.value = userEmail == "a" && userPassword == "a"
    }
}
