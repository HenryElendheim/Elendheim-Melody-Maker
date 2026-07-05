package com.elendheim.melodymaker.music

/**
 * Notes are represented as MIDI numbers (60 = C4, 69 = A4 = 440 Hz).
 */
object Note {
    val NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val FLAT_NAMES = arrayOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    const val MIN_MIDI = 36 // C2
    const val MAX_MIDI = 96 // C7

    fun name(midi: Int, useFlats: Boolean = false): String =
        (if (useFlats) FLAT_NAMES else NAMES)[midi.mod(12)] + (midi / 12 - 1)

    fun frequency(midi: Int): Double = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)
}

/** One note of a melody: a pitch plus a length in beats. */
data class NoteEvent(val midi: Int, val beats: Double) {
    val name: String get() = Note.name(midi)

    /** Human label for the note length: an eighth is "normal". */
    val lengthLabel: String
        get() = when {
            beats < 0.5 -> "short"
            beats > 0.5 -> "long"
            else -> "normal"
        }
}
