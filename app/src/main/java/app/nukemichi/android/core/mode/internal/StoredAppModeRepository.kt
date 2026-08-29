package app.nukemichi.android.core.mode.internal

import app.nukemichi.android.core.mode.AppMode
import app.nukemichi.android.core.mode.AppModeRepository
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.ExperienceKeys
import app.nukemichi.android.core.storage.StorageDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StoredAppModeRepository @Inject constructor(
    private val appStorage: AppStorage,
) : AppModeRepository {

    private val _mode = MutableStateFlow(readPersistedMode())
    override val mode: StateFlow<AppMode> = _mode.asStateFlow()

    override fun setMode(mode: AppMode) {
        appStorage.putString(StorageDomain.EXPERIENCE, ExperienceKeys.ADVANCED_MODE_ENABLED, mode.toStoredValue())
        _mode.value = mode
    }

    private fun readPersistedMode(): AppMode {
        val stored = appStorage.getString(StorageDomain.EXPERIENCE, ExperienceKeys.ADVANCED_MODE_ENABLED)
        return if (stored == ADVANCED_VALUE) AppMode.ADVANCED else AppMode.NORMAL
    }

    private fun AppMode.toStoredValue(): String = if (this == AppMode.ADVANCED) ADVANCED_VALUE else NORMAL_VALUE

    private companion object {
        const val ADVANCED_VALUE = "true"
        const val NORMAL_VALUE = "false"
    }
}
