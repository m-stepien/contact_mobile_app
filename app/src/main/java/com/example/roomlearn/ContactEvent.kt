package com.example.roomlearn

interface ContactEvent {
    object saveContact :ContactEvent
    data class SetFirstName(val firstName: String): ContactEvent
    data class SetLastName(val lastName: String): ContactEvent
    data class SetPhoneNumber(val phoneNumber: String): ContactEvent

    object showDialog: ContactEvent
    object HideDialog: ContactEvent

    data class SortContact(val sortType: SortType): ContactEvent
    data class DeleteContact(val contact: Contact): ContactEvent

}