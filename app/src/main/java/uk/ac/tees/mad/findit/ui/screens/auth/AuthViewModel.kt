package uk.ac.tees.mad.findit.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.findit.model.User
import uk.ac.tees.mad.findit.utils.Resource
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val authState: StateFlow<Resource<String>> = _authState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _authState.value = Resource.Success("Sign in successful")
                }
                .addOnFailureListener { exception ->
                    _authState.value = Resource.Error(exception.message ?: "Sign in failed")
                }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { user ->
                    firestore.collection("users").document(user.user?.uid ?: "").set(
                        hashMapOf(
                            "email" to email
                        )
                    )
                    _authState.value = Resource.Success("Sign up successful")
                }
                .addOnFailureListener { exception ->
                    _authState.value = Resource.Error(exception.message ?: "Sign up failed")
                }
        }
    }
}