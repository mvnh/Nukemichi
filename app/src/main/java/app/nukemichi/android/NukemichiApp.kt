package app.nukemichi.android

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.security.Provider
import java.security.Security

@HiltAndroidApp
class NukemichiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        plantTimberTree()
        setupBouncyCastle()
    }
}

private fun plantTimberTree() {
    if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
        return
    }

    Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority < Log.WARN) return
            Log.println(priority, tag ?: "Nukemichi", message)
            if (t != null) {
                Log.println(
                    priority,
                    tag ?: "Nukemichi",
                    Log.getStackTraceString(t)
                )
            }
        }
    })
}

private fun setupBouncyCastle() {
    val provider: Provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) ?: return
    if (provider.javaClass.equals(BouncyCastleProvider::class.java)) {
        return
    }
    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    Security.insertProviderAt(BouncyCastleProvider(), 1)
}
