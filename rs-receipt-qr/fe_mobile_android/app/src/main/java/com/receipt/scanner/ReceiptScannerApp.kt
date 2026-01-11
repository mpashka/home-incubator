package com.receipt.scanner

import android.app.Application

class ReceiptScannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ReceiptScannerApp
            private set
    }
}
