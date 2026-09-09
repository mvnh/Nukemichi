package app.nukemichi.android.core.ssh.di

import app.nukemichi.android.core.ssh.SshManager
import app.nukemichi.android.core.ssh.internal.SshjManager
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.platform.di.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object SshModule {

    @Provides
    @Singleton
    fun provideSshManager(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        appStorage: AppStorage
    ): SshManager = SshjManager(ioDispatcher, appStorage)
}
