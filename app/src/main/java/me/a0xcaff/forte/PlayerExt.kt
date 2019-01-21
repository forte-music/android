package me.a0xcaff.forte

import com.google.android.exoplayer2.Player

/**
 * Go the the previous song. If there is no previous song, and the current song is seekable, go back to the beginning of
 * the current track.
 */
fun Player.previousOrBeginning() {
    if (hasPrevious()) {
        previous()
    } else if (isCurrentWindowSeekable) {
        seekTo(0)
    }
}
