package uk.ac.tees.mad.findit.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    fun checkAuthState(
        onAuthenticated: () -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        if (auth.currentUser != null) {
            onAuthenticated()
        } else {
            onUnauthenticated()
        }
    }
}