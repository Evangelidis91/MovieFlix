package com.evangelidis.movieflix.presentation

import java.text.SimpleDateFormat
import java.util.Locale

/** Format TMDB date to display it */
fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("d MMM yyyy", Locale.US)
        parser.parse(this)?.let { formatter.format(it) } ?: this
    } catch (e: Exception){
        this
    }
}

/** Format rating to 1 decimal */
fun Double?.toRatingText(): String {
    if (this == null || this <= 0.0) return ""
    return String.format(Locale.US, "%.1f", this)
}

/** Format movie runtime */
fun Int?.toRuntimeText(): String {
    if (this == null || this <=0) return ""
    val hours = this/60
    val mins = this % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
