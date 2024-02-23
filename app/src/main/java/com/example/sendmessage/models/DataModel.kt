package com.example.sendmessage.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class  DataModel : ViewModel() {
    val message: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var chosenContacts: MutableMap<Int, Boolean> = mutableMapOf()
    lateinit var clearFunction: () -> Unit
}