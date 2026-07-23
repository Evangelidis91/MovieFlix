package com.evangelidis.movieflix.presentation

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.function.DoubleUnaryOperator

/** Format TMDB date to display it */
fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        parser.parse(this)?.let { formatter.format(it) } ?: this
    } catch (e: Exception){
        this
    }
}

/** Format rating to 1 decimal */
fun Double.toRatingText(): String = String.format(Locale.getDefault(), "%.1f", this)
