package com.example.sendmessage

import java.io.Serializable

data class Contact(
    var id: Int,
    var name: String,
    var phones: MutableMap<String, String>?,
    var emails: MutableMap<String, String>?,
    var photoUri: String?,
    var chosen: Boolean = false
) : Serializable
