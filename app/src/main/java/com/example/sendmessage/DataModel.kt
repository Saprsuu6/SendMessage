package com.example.sendmessage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DataModel : ViewModel() {
    val message: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    lateinit var chosenContacts: MutableMap<Int, Boolean>
    lateinit var clearFunction: () -> Unit
}