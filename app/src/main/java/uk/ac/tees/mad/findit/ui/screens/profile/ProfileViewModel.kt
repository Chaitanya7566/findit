package uk.ac.tees.mad.findit.ui.screens.profile


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.model.Location
import uk.ac.tees.mad.findit.model.User
import uk.ac.tees.mad.findit.utils.Resource
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _user = MutableStateFlow<Resource<User?>>(Resource.Idle())
    val user: StateFlow<Resource<User?>> = _user

    private val _userItems = MutableStateFlow<List<Item>>(emptyList())
    val userItems: StateFlow<List<Item>> = _userItems

    init {
        fetchUserProfile()
        fetchUserItems()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _user.value = Resource.Loading()
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val document = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    val user = document.toObject(User::class.java) ?: User(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "Anonymous",
                        email = currentUser.email ?: "",
                        phone = currentUser.phoneNumber ?: "",
                    )
                    _user.value = Resource.Success(user)
                } else {
                    _user.value = Resource.Error("No user signed in")
                }
            } catch (e: Exception) {
                _user.value = Resource.Error(e.message ?: "Failed to fetch profile")
            }
        }
    }

    private fun fetchUserItems() {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val documents = firestore.collection("items")
                        .whereEqualTo("postedId", currentUser.uid)
                        .get()
                        .await()

                    val items = documents.documents.mapNotNull {
                        val data = it.data
                        if (data != null) {
                            Item(
                                id = it.id,
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                category = data["category"] as? String ?: "",
                                imageUrl = data["imageUrl"] as? String ?: "",
                                lastSeenLocation = data["lastSeenLocation"] as? Location
                                    ?: Location(0.0, 0.0, ""),
                                status = ItemStatus.valueOf(data["status"] as String),
                                createdAt = data["createdAt"] as? Long ?: 0L,
                                postedId = data["postedId"] as? String ?: "",
                                posterEmail = data["posterEmail"] as? String ?: "",
                                posterPhone = data["posterPhone"] as? String ?: ""
                            )
                        } else {
                            null
                        }

                    }
                    _userItems.value = items
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to fetch user items", e)
            }
        }
    }
}