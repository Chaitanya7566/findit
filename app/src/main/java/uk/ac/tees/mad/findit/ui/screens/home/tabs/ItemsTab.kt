package uk.ac.tees.mad.findit.ui.screens.home.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.model.Location
import uk.ac.tees.mad.findit.ui.screens.home.components.ItemCard


@Composable
fun ItemsTab(
    itemType: ItemStatus,
    searchQuery: String,
    onItemClick: (String) -> Unit
) {
    // Mock data
    val items = remember {
        listOf(
            Item(
                id = "1",
                title = "Lost Keys",
                description = "Car and house keys with a blue keychain",
                category = "Personal Items",
                imageUrl = "",
                lastSeenLocation = Location(
                    latitude = 40.7128,
                    longitude = -74.0060,
                    address = "Central Park, New York"
                ),
                status = ItemStatus.LOST
            ),
            Item(
                id = "2",
                title = "Found Wallet",
                description = "Brown leather wallet with ID cards",
                category = "Personal Items",
                imageUrl = "",
                lastSeenLocation = Location(
                    latitude = 34.0522,
                    longitude = -118.2437,
                    address = "Downtown LA, California"
                ),
                status = ItemStatus.FOUND
            ),
            Item(
                id = "3",
                title = "Lost Phone",
                description = "Black iPhone 13 with a cracked screen",
                category = "Electronics",
                imageUrl = "",
                lastSeenLocation = Location(
                    latitude = 51.5074,
                    longitude = -0.1278,
                    address = "London Bridge, UK"
                ),
                status = ItemStatus.LOST
            )
        ).filter { it.status == itemType } // Filtering based on LOST or FOUND
    }

    val filteredItems = items.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.description.contains(searchQuery, ignoreCase = true) ||
        it.lastSeenLocation.address.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }
    }
}