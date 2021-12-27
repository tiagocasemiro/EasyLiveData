package com.easylivedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.m4u.ayla.annotation.Callback
import com.m4u.ayla.annotation.Delegate
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

@Delegate
class MainActivity : AppCompatActivity() {
    val mainViewModel: MainViewModel by inject { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel.event()
    }

    @Callback
    fun onResult() {
        println("Event handled with success")
    }
}