package com.evangelidis.movieflix

import com.evangelidis.movieflix.presentation.toDisplayDate
import com.evangelidis.movieflix.presentation.toRatingText
import com.evangelidis.movieflix.presentation.toRuntimeText
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Unit tests for UI text formatting extension functions (dates, ratings, and runtimes).
 * Verifies formatting logic against regular inputs and special cases like null or zero.
 */

class FormattersTest {

    @Test
    fun `toRatingText formats double to one decimal string`() {
        assertEquals("8.4", 8.4123.toRatingText())
        assertEquals("7.0", 7.0.toRatingText())
        assertEquals("", 0.0.toRatingText())
        assertEquals("", null.toRatingText())
    }

    @Test
    fun `toRuntimeText formats minutes to hours and minutes string`() {
        assertEquals("2h 29m", 149.toRuntimeText())
        assertEquals("1h 30m", 90.toRuntimeText())
        assertEquals("", 0.toRuntimeText())
        assertEquals("", null.toRuntimeText())
    }

    @Test
    fun `toDisplayDate formats API date string correctly`() {
        assertEquals("16 Jul 2010", "2010-07-16".toDisplayDate())
        assertEquals("", "".toDisplayDate())
        assertEquals("", null.toDisplayDate())
    }
}
