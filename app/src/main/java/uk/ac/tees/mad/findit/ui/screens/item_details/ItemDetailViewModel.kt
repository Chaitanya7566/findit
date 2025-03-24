package uk.ac.tees.mad.findit.ui.screens.item_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.repository.ItemRepository
import uk.ac.tees.mad.findit.utils.Resource
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _item = MutableStateFlow<Resource<Item>>(Resource.Idle())
    val item: StateFlow<Resource<Item>> = _item.asStateFlow()

    private val _updateStatus = MutableStateFlow<Resource<Item>>(Resource.Idle())
    val updateStatus = _updateStatus.asStateFlow()

    fun fetchItem(itemId: String) {
        viewModelScope.launch {
            repository.getItemById(itemId).collect {
                _item.value = it
            }
        }
    }

    fun claimItem(item: Item) {
        viewModelScope.launch {
            repository.claimItem(item).collect {
                _updateStatus.value = it
                _item.value = it
            }
        }
    }
}
