package app.nukemichi.android.feature.dashboard.impl.ui.screen.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun Long.toDisplayDate(): String = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(this))
