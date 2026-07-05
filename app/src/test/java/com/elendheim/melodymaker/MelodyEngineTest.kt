package com.elendheim.melodymaker

import com.elendheim.melodymaker.music.ChordType
import com.elendheim.melodymaker.music.MelodyEngine
import com.elendheim.melodymaker.music.Note
import com.elendheim.melodymaker.music.RhythmFeel
import com.elendheim.melodymaker.music.RollSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MelodyEngineTest {

    private fun settings(
        low: Int = 60,
        high: Int = 72,
        count: Int = 6,
        rhythm: RhythmFeel = RhythmFeel.EVEN,
        smoothness: Float = 0.5f,
        chordLock: Boolean = false,
        root: Int = 0,
        type: ChordType = ChordType.MAJOR
    ) = RollSettings(low, high, count, rhythm, smoothness, chordLock, root, type)

    @Test
    fun noteNamesMatchMidi() {
        assertEquals("C4", Note.name(60))
        assertEquals("A4", Note.name(69))
        assertEquals("C5", Note.name(72))
        assertEquals("F#3", Note.name(54))
    }

    @Test
    fun a4Is440Hz() {
        assertEquals(440.0, Note.frequency(69), 0.001)
    }

    @Test
    fun generateRespectsRangeAndCount() {
        val random = Random(42)
        repeat(200) {
            val melody = MelodyEngine.generate(settings(), random)
            assertEquals(6, melody.size)
            melody.forEach { assertTrue(it.midi in 60..72) }
        }
    }

    @Test
    fun evenRhythmIsAllEqual() {
        val melody = MelodyEngine.generate(settings(rhythm = RhythmFeel.EVEN), Random(1))
        assertTrue(melody.all { it.beats == 0.5 })
    }

    @Test
    fun chordLockOnlyProducesChordTones() {
        val random = Random(7)
        val allowed = ChordType.MINOR7.pitchClasses(9) // A minor 7: A C E G
        assertEquals(setOf(9, 0, 4, 7), allowed)
        repeat(100) {
            val melody = MelodyEngine.generate(
                settings(chordLock = true, root = 9, type = ChordType.MINOR7),
                random
            )
            melody.forEach { assertTrue(it.midi.mod(12) in allowed) }
        }
    }

    @Test
    fun chordLockOverNarrowRangeFallsBackToRange() {
        // C#4 to D#4 contains no C major tones; the pool must not be empty.
        val pool = MelodyEngine.pitchPool(
            settings(low = 61, high = 63, chordLock = true, root = 0, type = ChordType.MAJOR)
        )
        assertEquals(listOf(61, 62, 63), pool)
    }

    @Test
    fun mutateChangesExactlyOnePitchAndKeepsRhythm() {
        val random = Random(3)
        repeat(100) {
            val melody = MelodyEngine.generate(settings(), random)
            val mutated = MelodyEngine.mutate(melody, settings(), random)
            assertEquals(melody.size, mutated.size)
            val changed = melody.zip(mutated).count { (a, b) -> a.midi != b.midi }
            assertEquals(1, changed)
            melody.zip(mutated).forEach { (a, b) -> assertEquals(a.beats, b.beats, 0.0) }
            mutated.forEach { assertTrue(it.midi in 60..72) }
        }
    }

    @Test
    fun highSmoothnessProducesSmallerAverageIntervals() {
        val random = Random(11)
        fun meanInterval(smoothness: Float): Double {
            var total = 0.0
            var steps = 0
            repeat(300) {
                val melody = MelodyEngine.generate(
                    settings(low = 48, high = 84, count = 8, smoothness = smoothness),
                    random
                )
                melody.zipWithNext().forEach { (a, b) ->
                    total += Math.abs(a.midi - b.midi)
                    steps++
                }
            }
            return total / steps
        }
        val loose = meanInterval(0f)
        val tight = meanInterval(1f)
        assertTrue("expected $tight < $loose", tight < loose)
    }
}
