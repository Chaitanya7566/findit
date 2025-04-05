package uk.ac.tees.mad.findit.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.model.Location
import uk.ac.tees.mad.findit.model.User
import uk.ac.tees.mad.findit.utils.Resource
import java.io.File
import java.io.FileInputStream
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _user = MutableStateFlow<Resource<User?>>(Resource.Idle())
    val user: StateFlow<Resource<User?>> = _user

    private val _userItems = MutableStateFlow<List<Item>>(emptyList())
    val userItems: StateFlow<List<Item>> = _userItems

    private val _updateStatus = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val updateStatus: StateFlow<Resource<Unit>> = _updateStatus

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
                    val data = document.data


                    val user = if (data != null) {
                        User(
                            id = currentUser.uid,
                            name = data["name"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            phone = data["phone"] as? String ?: "",
                            profilePictureUrl = data["profilePictureUrl"] as? String ?: ""
                        )
                    } else {
                        User(
                            id = currentUser.uid,
                            name = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            phone = currentUser.phoneNumber ?: "",
                            profilePictureUrl = ""
                        )
                    }
                    _user.value = Resource.Success(user)
                } else {
                    _user.value = Resource.Error("No user signed in")
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                e.printStackTrace()
                Log.e("ProfileViewModel", "Failed to fetch user items", e)
            }
        }
    }

    fun updateProfile(updatedUser: User, imageFile: File?) {
        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userRef = firestore.collection("users").document(currentUser.uid)
                    val profileUpdates = mutableMapOf<String, Any>(
                        "name" to updatedUser.name,
                        "email" to updatedUser.email
                    )

                    updatedUser.phone.let { profileUpdates["phone"] = it }

                    if (imageFile != null) {
                        val imageBytes = withContext(Dispatchers.IO) {
                            FileInputStream(imageFile).use { it.readBytes() }
                        }
                        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
                        profileUpdates["profilePictureUrl"] = base64Image
                    }

                    userRef.set(profileUpdates, SetOptions.merge()).await()

                    val updatedUserData = updatedUser.copy(
                        profilePictureUrl = if (imageFile != null)
                            profileUpdates["profilePictureUrl"] as String
                        else
                            updatedUser.profilePictureUrl
                    )
                    _user.value = Resource.Success(updatedUserData)
                    _updateStatus.value = Resource.Success(Unit)
                }
            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun updateItem(item: Item, imageFile: File?) {
        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()
            try {
                val itemRef = firestore.collection("items").document(item.id)
                val itemUpdates = mutableMapOf<String, Any>(
                    "title" to item.title,
                    "description" to item.description,
                    "category" to item.category
                )

                if (imageFile != null) {
                    val imageBytes = withContext(Dispatchers.IO) {
                        FileInputStream(imageFile).use { it.readBytes() }
                    }
                    val base64Image = Base64.getEncoder().encodeToString(imageBytes)
                    itemUpdates["imageUrl"] = base64Image
                }

                itemRef.set(itemUpdates, SetOptions.merge()).await()

                val updatedItems = _userItems.value.map {
                    if (it.id == item.id) {
                        item.copy(imageUrl = if (imageFile != null) itemUpdates["imageUrl"] as String else it.imageUrl)
                    } else it
                }
                _userItems.value = updatedItems
                _updateStatus.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.message ?: "Failed to update item")
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()
            try {
                firestore.collection("items").document(itemId).delete().await()
                _userItems.value = _userItems.value.filter { it.id != itemId }
                _updateStatus.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.message ?: "Failed to delete item")
            }
        }
    }


    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}