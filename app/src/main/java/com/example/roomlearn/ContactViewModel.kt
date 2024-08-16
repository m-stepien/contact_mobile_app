package com.example.roomlearn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModel(private val dao: ContactDao) : ViewModel(){
    private val _sortType = MutableStateFlow(SortType.LAST_NAME)
    private val _state = MutableStateFlow(ContactState())
    private val _contacts = _sortType.flatMapLatest  {sortType ->
        Log.d("ContactViewModel", "SortType changed: $sortType")
        when(sortType){
            SortType.FIRST_NAME -> {
                Log.d("ContactViewModel", "Fetching contacts ordered by first name")
                dao.getContactOrderedByFirstName()
            }
            SortType.LAST_NAME -> {
                Log.d("ContactViewModel", "Fetching contacts ordered by last name")
                dao.getContactOrderedByLastName()
            }
            SortType.PHONE_NUMBER ->
            {
                Log.d("ContactViewModel", "Fetching contacts ordered by phone number")
                dao.getContactOrderedByPhoneNumber()
            }
        }
    }    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        .also {
            Log.d("ContactViewModel", "Contacts flow initialized")
        }
    val state = combine(_state,_sortType, _contacts){

        state, sortType, contacts ->
        Log.d("ContactViewModel", "test")
        Log.d("ContactViewModel", "State updated with contacts sorted by: $sortType $contacts $state")
        state.copy(
            contacts = contacts,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactState())
    fun onEvent(event: ContactEvent){
        when(event){
            is ContactEvent.DeleteContact ->{
                viewModelScope.launch {
                dao.deleteContact(event.contact)
                }
            }
            ContactEvent.HideDialog -> {
                _state.update {
                    it.copy(isAddingContact = false)
                }

            }
            ContactEvent.saveContact -> {
                val firstName = state.value.firstName
                val lastName = state.value.lastName
                val phoneNumber = state.value.phoneNumber

                if(firstName.isBlank() || lastName.isBlank() || phoneNumber.isBlank()){
                    return
                }
                val contact = Contact(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber
                )
                viewModelScope.launch {
                    dao.insertContact(contact)
                }
                _state.update {
                    it.copy(
                        isAddingContact = false,
                        firstName = "",
                        lastName = "",
                        phoneNumber = ""
                    )
                }
            }
            is ContactEvent.SetFirstName -> {
                Log.d("ContactViewModel", "Setting first name: ${event.firstName}")
                _state.update { it.copy(
                    firstName = event.firstName
                ) }
            }
            is ContactEvent.SetLastName -> {
                Log.d("ContactViewModel", "Setting last name: ${event.lastName}")
                _state.update {
                    it.copy(
                        lastName = event.lastName
                    )
                }
            }
            is ContactEvent.SetPhoneNumber -> {
                Log.d("ContactViewModel", "Setting phone number: ${event.phoneNumber}")
                _state.update {
                    it.copy(
                        phoneNumber = event.phoneNumber
                    )
                }
            }
            ContactEvent.showDialog -> {
                _state.update {
                    it.copy(
                        isAddingContact = true
                    )
                }
            }
            is ContactEvent.SortContact -> {
                Log.d("ContactViewModel", "Setting sort type: ${event.sortType}")
                _sortType.value = event.sortType
            }
        }
    }
}