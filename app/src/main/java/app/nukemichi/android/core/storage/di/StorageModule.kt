package app.nukemichi.android.core.storage.di

import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.internal.AppStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class StorageModule {
    @Binds
    abstract fun bindAppStorage(impl: AppStorageImpl): AppStorage
}
