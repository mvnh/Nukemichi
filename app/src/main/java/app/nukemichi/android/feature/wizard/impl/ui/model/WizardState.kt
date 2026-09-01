package app.nukemichi.android.feature.wizard.impl.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class WizardState(
    initialPage: Int,
    val pageCount: Int
) {
    var currentPage by mutableIntStateOf(initialPage)
        private set

    val isFirstPage get() = currentPage == 0
    val isLastPage get() = currentPage == pageCount - 1

    fun navigateTo(page: Int) {
        if (page in 0 until pageCount) {
            currentPage = page
        }
    }

    fun next() = navigateTo(currentPage + 1)
    fun previous() = navigateTo(currentPage - 1)
}

@Composable
internal fun rememberWizardState(initialPage: Int = 0, pageCount: Int): WizardState {
    return rememberSaveable(
        saver = listSaver(
            save = { listOf(it.currentPage, it.pageCount) },
            restore = { WizardState(initialPage = it[0], pageCount = it[1]) }
        )
    ) {
        WizardState(initialPage, pageCount)
    }
}
