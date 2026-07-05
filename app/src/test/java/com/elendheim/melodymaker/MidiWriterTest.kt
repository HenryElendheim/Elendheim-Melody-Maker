package com.elendheim.melodymaker

import com.elendheim.melodymaker.music.MidiWriter
import com.elendheim.melodymaker.music.NoteEvent
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MidiWriterTest {

    private fun Int.b() = toByte()

    @Test
    fun vlqEncodesSingleAndMultiByteValues() {
        assertArrayEquals(byteArrayOf(0x00), MidiWriter.vlq(0))
        assertArrayEquals(byteArrayOf(0x7F), MidiWriter.vlq(127))
        assertArrayEquals(byteArrayOf(0x81.b(), 0x00), MidiWriter.vlq(128))
        assertArrayEquals(byteArrayOf(0x81.b(), 0x70), MidiWriter.vlq(240))
        assertArrayEquals(byteArrayOf(0xC0.b(), 0x00), MidiWriter.vlq(8192))
    }

    @Test
    fun headerIsValidFormatZeroFile() {
        val bytes = MidiWriter.write(listOf(NoteEvent(60, 0.5)), 120)
        // "MThd", length 6, format 0, one track, 480 ticks per quarter
        assertArrayEquals(
            byteArrayOf(
                0x4D, 0x54, 0x68, 0x64,
                0x00, 0x00, 0x00, 0x06,
                0x00, 0x00,
                0x00, 0x01,
                0x01, 0xE0.b()
            ),
            bytes.copyOfRange(0, 14)
        )
        assertArrayEquals(byteArrayOf(0x4D, 0x54, 0x72, 0x6B), bytes.copyOfRange(14, 18))
    }

    @Test
    fun trackLengthFieldMatchesActualTrackSize() {
        val bytes = MidiWriter.write(
            listOf(NoteEvent(60, 0.5), NoteEvent(64, 0.25), NoteEvent(67, 1.0)),
            100
        )
        val declared = ((bytes[18].toInt() and 0xFF) shl 24) or
            ((bytes[19].toInt() and 0xFF) shl 16) or
            ((bytes[20].toInt() and 0xFF) shl 8) or
            (bytes[21].toInt() and 0xFF)
        assertEquals(bytes.size - 22, declared)
    }

    @Test
    fun tempoEventEncodesBpm() {
        val bytes = MidiWriter.write(listOf(NoteEvent(60, 0.5)), 120)
        // First track event: delta 0, FF 51 03, then 500000 microseconds per quarter.
        val event = bytes.copyOfRange(22, 29)
        assertArrayEquals(
            byteArrayOf(0x00, 0xFF.b(), 0x51, 0x03, 0x07, 0xA1.b(), 0x20),
            event
        )
    }

    @Test
    fun noteEventsUseCorrectPitchAndDuration() {
        val bytes = MidiWriter.write(listOf(NoteEvent(60, 0.5)), 120)
        val hex = bytes.joinToString("") { "%02X".format(it) }
        // Note on C4 velocity 96, then delta 240 ticks (81 70), note off.
        assertEquals(true, hex.contains("00903C60"))
        assertEquals(true, hex.contains("8170803C00"))
        // Ends with end-of-track meta.
        assertEquals(true, hex.endsWith("00FF2F00"))
    }
}
