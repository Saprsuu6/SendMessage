package com.example.sendmessage

data class Contact(
    var id: Int,
    var name: String,
    var phones: MutableMap<String, String>?,
    var emails: MutableMap<String, String>?,
    var photoUri: String?,
    var chosen: Boolean = false
)
