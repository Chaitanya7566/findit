package uk.ac.tees.mad.findit.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.model.Location
import uk.ac.tees.mad.findit.utils.Resource
import javax.inject.Inject

class ItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getItemsByStatus(status: ItemStatus): Flow<Resource<List<Item>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("items")
                .whereEqualTo("status", status.name)
//                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val items = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    val lastSeenLocation = data["lastSeenLocation"] as? HashMap<String, Any>
                    val location = if (lastSeenLocation != null) {
                        Location(
                            latitude = lastSeenLocation["latitude"] as Double,
                            longitude = lastSeenLocation["longitude"] as Double,
                            address = lastSeenLocation["address"] as String
                        )
                    } else {
                        Location(0.0, 0.0, "")
                    }
                    Item(
                        id = doc.id,
                        title = data["title"] as String,
                        description = data["description"] as String,
                        category = data["category"] as String,
                        imageUrl = data["imageUrl"] as String,
                        lastSeenLocation = location,
                        status = ItemStatus.valueOf(data["status"] as String),
                        createdAt = data["createdAt"] as Long
                    )
                } else {
                    null
                }
            }
            emit(Resource.Success(items))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(e.message ?: "Failed to fetch items"))
        }
    }


    fun getItemById(id: String): Flow<Resource<Item>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("items")
                .document(id)
                .get()
                .await()
            val data = snapshot.data ?: throw Exception("Item not found")

            val lastSeenLocation = data["lastSeenLocation"] as? HashMap<String, Any>
            val location = if (lastSeenLocation != null) {
                Location(
                    latitude = lastSeenLocation["latitude"] as Double,
                    longitude = lastSeenLocation["longitude"] as Double,
                    address = lastSeenLocation["address"] as String
                )
            } else {
                Location(0.0, 0.0, "")
            }
            val item = Item(
                id = snapshot.id,
                title = data["title"] as String,
                description = data["description"] as String,
                category = data["category"] as String,
                imageUrl = data["imageUrl"] as String,
                lastSeenLocation = location,
                status = ItemStatus.valueOf(data["status"] as String),
                createdAt = data["createdAt"] as Long
            )

            emit(Resource.Success(item))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(e.message ?: "Failed to fetch items"))
        }
    }
}