package me.a0xcaff.forte.ui.connect

import junit.framework.Assert.assertEquals
import me.a0xcaff.forte.ui.formatTime
import org.junit.Test

class CurrentTimeTextViewTest {
    @Test
    fun `a few seconds`() =
        assertEquals("0:04", formatTime(4240))

    @Test
    fun `double digit seconds`() =
        assertEquals("0:13", formatTime(13620))

    @Test
    fun `single digit minutes`() =
        assertEquals("1:34", formatTime(94420))

    @Test
    fun `double digit minutes`() =
        assertEquals("14:52", formatTime(892000))

    @Test
    fun `over an hour`() =
        assertEquals("1:23:42", formatTime(5022000))

    @Test
    fun `many hours`() =
        assertEquals("143:32:12", formatTime(516732000))
}
