package uk.ac.tees.mad.findit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.findit.model.Item
import uk.ac.tees.mad.findit.model.ItemStatus
import uk.ac.tees.mad.findit.repository.ItemRepository
import uk.ac.tees.mad.findit.utils.Resource
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _lostItems = MutableStateFlow<Resource<List<Item>>>(Resource.Loading())
    val lostItems: StateFlow<Resource<List<Item>>> = _lostItems

    private val _foundItems = MutableStateFlow<Resource<List<Item>>>(Resource.Loading())
    val foundItems: StateFlow<Resource<List<Item>>> = _foundItems

    init {
        fetchItems()
    }

    private fun fetchItems() {
        viewModelScope.launch {
            repository.getItemsByStatus(ItemStatus.LOST).collect { _lostItems.value = it }
            repository.getItemsByStatus(ItemStatus.FOUND).collect { _foundItems.value = it }
        }
    }
}