package com.example.sendmessage

import java.util.Date

data class Report(
    val key: Int? = null,
    val name: String,
    val phoneNumbers: String,
    val emails: String,
    val photoUri: String?,
    val dateSend: Date
)
