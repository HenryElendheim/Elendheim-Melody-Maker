package com.elendheim.melodymaker.music

import java.io.ByteArrayOutputStream

/**
 * Writes a melody as a Standard MIDI File (format 0, single track) that
 * imports cleanly into FL Studio and every other DAW.
 */
object MidiWriter {

    const val TICKS_PER_QUARTER = 480
    private const val VELOCITY = 96

    fun write(events: List<NoteEvent>, bpm: Int): ByteArray {
        val track = ByteArrayOutputStream()

        // Tempo meta event: microseconds per quarter note.
        val microsPerQuarter = 60_000_000 / bpm.coerceAtLeast(1)
        track.writeVlq(0)
        track.write(byteArrayOf(0xFF.toByte(), 0x51, 0x03))
        track.write(microsPerQuarter ushr 16 and 0xFF)
        track.write(microsPerQuarter ushr 8 and 0xFF)
        track.write(microsPerQuarter and 0xFF)

        // 4/4 time signature meta event.
        track.writeVlq(0)
        track.write(byteArrayOf(0xFF.toByte(), 0x58, 0x04, 0x04, 0x02, 0x18, 0x08))

        for (event in events) {
            val ticks = (event.beats * TICKS_PER_QUARTER).toInt().coerceAtLeast(1)
            track.writeVlq(0)
            track.write(byteArrayOf(0x90.toByte(), event.midi.toByte(), VELOCITY.toByte()))
            track.writeVlq(ticks)
            track.write(byteArrayOf(0x80.toByte(), event.midi.toByte(), 0x00))
        }

        // End of track.
        track.writeVlq(0)
        track.write(byteArrayOf(0xFF.toByte(), 0x2F, 0x00))

        val trackBytes = track.toByteArray()
        val out = ByteArrayOutputStream()
        out.write("MThd".toByteArray(Charsets.US_ASCII))
        out.writeInt32(6)
        out.writeInt16(0) // format 0
        out.writeInt16(1) // one track
        out.writeInt16(TICKS_PER_QUARTER)
        out.write("MTrk".toByteArray(Charsets.US_ASCII))
        out.writeInt32(trackBytes.size)
        out.write(trackBytes)
        return out.toByteArray()
    }

    /** Variable-length quantity encoding, as required for MIDI delta times. */
    fun vlq(value: Int): ByteArray {
        var v = value
        val stack = ArrayList<Int>(4)
        stack.add(v and 0x7F)
        v = v ushr 7
        while (v > 0) {
            stack.add(v and 0x7F or 0x80)
            v = v ushr 7
        }
        return ByteArray(stack.size) { stack[stack.size - 1 - it].toByte() }
    }

    private fun ByteArrayOutputStream.writeVlq(value: Int) = write(vlq(value))

    private fun ByteArrayOutputStream.writeInt16(value: Int) {
        write(value ushr 8 and 0xFF)
        write(value and 0xFF)
    }

    private fun ByteArrayOutputStream.writeInt32(value: Int) {
        write(value ushr 24 and 0xFF)
        write(value ushr 16 and 0xFF)
        write(value ushr 8 and 0xFF)
        write(value and 0xFF)
    }
}
