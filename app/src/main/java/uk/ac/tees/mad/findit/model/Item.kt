package uk.ac.tees.mad.findit.model

data class Item(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val lastSeenLocation: Location = Location(0.0, 0.0, ""),
    val status: ItemStatus = ItemStatus.LOST,
    val createdAt: Long = 0L,
    val postedId: String? = null,
    val posterEmail: String? = null,
    val posterPhone: String? = null
)