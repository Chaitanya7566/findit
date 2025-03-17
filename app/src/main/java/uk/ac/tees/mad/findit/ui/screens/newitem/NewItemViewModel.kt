package uk.ac.tees.mad.findit.ui.screens.newitem

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.findit.model.Location
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NewItemViewModel @Inject constructor(
    @ApplicationContext private val context: Context
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
}