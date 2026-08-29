package app.nukemichi.android.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

@Composable
fun <T> EffectHandler(
    effectFlow: Flow<T>,
    vararg keys: Any?,
    onEffect: (T) -> Unit
) {
    effectFlow.CollectAsEffect(keys = keys, onEffect = onEffect)
}

@Composable
fun <T> Flow<T>.CollectAsEffect(
    vararg keys: Any?,
    onEffect: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(this, lifecycleOwner, *keys) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                this@CollectAsEffect
                    .catch { throwable -> throwable.printStackTrace() }
                    .collect { effect ->
                        onEffect(effect)
                    }
            }
        }
    }
}