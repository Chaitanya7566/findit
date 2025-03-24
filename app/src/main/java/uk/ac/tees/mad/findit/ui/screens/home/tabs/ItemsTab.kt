package uk.ac.tees.mad.findit.ui.screens.home.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.ui.screens.home.HomeViewModel
import uk.ac.tees.mad.findit.ui.screens.home.components.ItemCard
import uk.ac.tees.mad.findit.utils.Resource

@Composable
fun ItemsTab(
    itemType: ItemStatus,
    searchQuery: String,
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val itemsState by (if (itemType == ItemStatus.LOST) viewModel.lostItems else viewModel.foundItems)
        .collectAsState()
    val filters by viewModel.filters.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf("") }
    var locationFilter by remember { mutableStateOf("") }
    var daysAgoFilter by remember { mutableStateOf("") }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Items") },
            text = {
                Column {
                    OutlinedTextField(
                        value = categoryFilter,
                        onValueChange = { categoryFilter = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = locationFilter,
                        onValueChange = { locationFilter = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = daysAgoFilter,
                        onValueChange = { daysAgoFilter = it },
                        label = { Text("Days Ago") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.applyFilters(
                        category = categoryFilter.takeIf { it.isNotEmpty() },
                        location = locationFilter.takeIf { it.isNotEmpty() },
                        daysAgo = daysAgoFilter.toIntOrNull()
                    )
                    showFilterDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (itemsState) {
            is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is Resource.Success -> {
                val items = (itemsState as Resource.Success).data ?: emptyList()
                val filteredItems = items.filter { item ->
                    val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                            item.description.contains(searchQuery, ignoreCase = true) ||
                            item.lastSeenLocation.address.contains(searchQuery, ignoreCase = true)
                    val matchesCategory =
                        filters.category?.let { item.category.contains(it, ignoreCase = true) }
                            ?: true
                    val matchesLocation = filters.location?.let {
                        item.lastSeenLocation.address.contains(
                            it,
                            ignoreCase = true
                        )
                    } ?: true
                    val matchesDate = filters.daysAgo?.let { days ->
                        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
                        item.createdAt >= cutoff
                    } ?: true
                    matchesSearch && matchesCategory && matchesLocation && matchesDate
                }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(onClick = { showFilterDialog = true }) {
                            Text("Filters")
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(onClick = { viewModel.fetchItems() }) {
                            Text("Refresh")
                        }
                    }
                    if (filteredItems.isEmpty()) {
                        Text(
                            text = "No ${itemType.name.lowercase()} items found",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredItems) { item ->
                                ItemCard(item = item, onClick = { onItemClick(item.id) })
                            }
                        }
                    }
                }
            }

            is Resource.Error -> Text(
                text = "Something went wrong",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )

            is Resource.Idle -> {}
        }
    }
}