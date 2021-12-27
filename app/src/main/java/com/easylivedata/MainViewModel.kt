package com.easylivedata

import androidx.lifecycle.ViewModel

class MainViewModel(private val delegate: MainDelegate): ViewModel() {


    fun event() {
        delegate.onResult()
    }
}