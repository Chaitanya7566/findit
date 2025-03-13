package uk.ac.tees.mad.findit.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
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
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val items = snapshot.toObjects(Item::class.java)
            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch items"))
        }
    }
}