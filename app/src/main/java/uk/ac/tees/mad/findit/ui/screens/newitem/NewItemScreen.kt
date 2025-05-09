package uk.ac.tees.mad.findit.ui.screens.newitem

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.model.Location
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NewItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewItemViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf(Location(0.0, 0.0, "")) }
    var status by remember { mutableStateOf(ItemStatus.LOST) }

    val locationState by viewModel.location.collectAsState()

    locationState?.let { location = it } // Update location when fetched
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val context = LocalContext.current

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val imagePath = imageUri?.let { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val file = File(context.cacheDir, "temp_image.jpg")
                        file.outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }
                        file.absolutePath
                    } catch (e: Exception) {
                        null
                    }
                }
                imagePath?.let { imageUrl = it }
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val imagePath = uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File(context.cacheDir, "temp_image.jpg")
                    file.outputStream().use { output ->
                        inputStream?.copyTo(output)
                    }
                    file.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
            imagePath?.let { imageUrl = it }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post New Item") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Category Field
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Status Selection
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = status == ItemStatus.LOST,
                        onClick = { status = ItemStatus.LOST }
                    )
                    Text("Lost")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = status == ItemStatus.FOUND,
                        onClick = { status = ItemStatus.FOUND }
                    )
                    Text("Found")
                }
            }

            // Location Field with Detection
            OutlinedTextField(
                value = location.address,
                onValueChange = { location = location.copy(address = it) },
                label = { Text("Last Seen Location") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (locationPermissionState.status.isGranted) {
                                viewModel.fetchCurrentLocation {
                                    // Permission denied
                                    Toast.makeText(
                                        context,
                                        "Location permission denied",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (locationPermissionState.status.shouldShowRationale) {
                                // Show rationale
                                Toast.makeText(
                                    context,
                                    "Location permission is needed to detect your location",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Get Current Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true
            )

            // Photo Upload Section
            Text(
                text = "Photo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            if (imageUrl.isNotEmpty()) {

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Uploaded Photo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            val tempFile =
                                File.createTempFile("upload_image", ".jpg", context.cacheDir)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                tempFile
                            )
                            imageUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Camera")
                }
                Button(
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Text("Gallery")
                }
            }

            // Post Button
            Button(
                onClick = {
                    val item = Item(
                        id = "",
                        title = title,
                        description = description,
                        category = category,
                        imageUrl = "",
                        lastSeenLocation = location,
                        status = status,
                        createdAt = 0L
                    )
                    if (imageUrl.isNotEmpty()) {
                        viewModel.postItem(
                            item = item,
                            imageFilePath = imageUrl,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Item posted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateBack()
                            },
                            onFailure = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Please add a photo", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                enabled = title.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty()
            ) {
                Text("Post Item", fontSize = 16.sp)
            }
        }
    }
}