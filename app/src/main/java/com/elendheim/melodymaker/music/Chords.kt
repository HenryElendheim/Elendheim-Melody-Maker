package com.elendheim.melodymaker.music

/** Chord qualities used by chord lock. Intervals are semitones above the root. */
enum class ChordType(val label: String, val intervals: List<Int>) {
    MAJOR("Major", listOf(0, 4, 7)),
    MINOR("Minor", listOf(0, 3, 7)),
    MAJOR7("Major 7", listOf(0, 4, 7, 11)),
    MINOR7("Minor 7", listOf(0, 3, 7, 10)),
    DOM7("Dominant 7", listOf(0, 4, 7, 10)),
    SUS4("Sus4", listOf(0, 5, 7));

    /** Pitch classes (0-11) of this chord built on the given root pitch class. */
    fun pitchClasses(rootPitchClass: Int): Set<Int> =
        intervals.map { (rootPitchClass + it).mod(12) }.toSet()
}
