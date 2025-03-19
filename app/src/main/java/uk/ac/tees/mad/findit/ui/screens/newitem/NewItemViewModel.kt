package uk.ac.tees.mad.findit.ui.screens.newitem

import android.content.Context
import android.location.Geocoder
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.Location
import java.io.File
import java.io.FileInputStream
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NewItemViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun fetchCurrentLocation(onPermissionDenied: () -> Unit) {
        viewModelScope.launch {
            try {
                val locationResult = fusedLocationClient.lastLocation.await()
                locationResult?.let { loc ->
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
                    _location.value = Location(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        address = address
                    )
                } ?: onPermissionDenied()
            } catch (e: SecurityException) {
                onPermissionDenied()
            }
        }
    }

    fun postItem(
        item: Item,
        imageFilePath: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Convert image file to Base64
                val imageFile = File(imageFilePath)
                val imageBytes = withContext(Dispatchers.IO) {
                    FileInputStream(imageFile).use { it.readBytes() }
                }
                val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                val itemId = firestore.collection("items").document().id
                val itemToPost = item.copy(
                    id = itemId,
                    imageUrl = base64Image, // Store Base64 string in imageUrl
                    createdAt = System.currentTimeMillis()
                )

                // Save to Firestore
                firestore.collection("items").document(itemId).set(itemToPost).await()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to post item")
                }
            }
        }
    }
}