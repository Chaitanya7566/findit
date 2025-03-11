package uk.ac.tees.mad.findit.model
data class Item(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val lastSeenLocation: Location,
    val status: ItemStatus
)