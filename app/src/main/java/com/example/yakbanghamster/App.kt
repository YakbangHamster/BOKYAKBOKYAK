package com.example.yakbanghamster

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        AndroidThreeTen.init(this)
    }
    companion object {
        lateinit var context: Context
            private set
    }
}
