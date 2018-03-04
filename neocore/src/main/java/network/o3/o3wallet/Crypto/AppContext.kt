package network.o3.o3wallet

import android.app.Application
import android.content.Context
/**
 * Created by drei on 11/29/17.
 */

class O3Wallet : Application() {
    override fun onCreate() {
        super.onCreate()
        O3Wallet.appContext = getApplicationContext()
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}