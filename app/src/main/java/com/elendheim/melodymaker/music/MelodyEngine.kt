package com.elendheim.melodymaker.music

import kotlin.math.abs
import kotlin.math.exp
import kotlin.random.Random

enum class RhythmFeel(val label: String) {
    EVEN("Even"),
    MIXED("Mixed")
}

/** Everything the generator needs to know, straight from the UI. */
data class RollSettings(
    val lowMidi: Int,
    val highMidi: Int,
    val noteCount: Int,
    val rhythm: RhythmFeel,
    /** 0 = pure random pitches, 1 = strongly favors stepwise motion. */
    val smoothness: Float,
    val chordLock: Boolean,
    val chordRootPitchClass: Int,
    val chordType: ChordType
)

object MelodyEngine {

    /** All allowed pitches for the current settings, lowest to highest. */
    fun pitchPool(settings: RollSettings): List<Int> {
        val range = settings.lowMidi..settings.highMidi
        val pool = if (settings.chordLock) {
            val allowed = settings.chordType.pitchClasses(settings.chordRootPitchClass)
            range.filter { it.mod(12) in allowed }
        } else {
            range.toList()
        }
        // A chord lock over a very narrow range can leave nothing; fall back to the raw range.
        return pool.ifEmpty { range.toList() }
    }

    fun generate(settings: RollSettings, random: Random = Random.Default): List<NoteEvent> {
        val pool = pitchPool(settings)
        val pitches = ArrayList<Int>(settings.noteCount)
        var previous = pool.random(random)
        pitches.add(previous)
        while (pitches.size < settings.noteCount) {
            previous = pickNext(pool, previous, settings.smoothness, random)
            pitches.add(previous)
        }
        val beats = rollRhythm(settings.noteCount, settings.rhythm, random)
        return pitches.zip(beats) { midi, b -> NoteEvent(midi, b) }
    }

    /**
     * Keeps the melody you liked but re-rolls exactly one note, respecting the
     * current constraints and leaning toward pitches that fit its neighbors.
     */
    fun mutate(
        melody: List<NoteEvent>,
        settings: RollSettings,
        random: Random = Random.Default
    ): List<NoteEvent> {
        if (melody.isEmpty()) return generate(settings, random)
        val pool = pitchPool(settings)
        val index = random.nextInt(melody.size)
        val current = melody[index].midi
        val candidates = pool.filter { it != current }.ifEmpty { pool }

        val prev = melody.getOrNull(index - 1)?.midi
        val next = melody.getOrNull(index + 1)?.midi
        val weights = candidates.map { candidate ->
            val distances = listOfNotNull(prev, next).map { abs(candidate - it) }
            val meanDistance = if (distances.isEmpty()) 0.0 else distances.average()
            smoothWeight(meanDistance, settings.smoothness)
        }
        val newMidi = weightedPick(candidates, weights, random)
        return melody.toMutableList().also {
            it[index] = it[index].copy(midi = newMidi)
        }
    }

    private fun pickNext(pool: List<Int>, previous: Int, smoothness: Float, random: Random): Int {
        val weights = pool.map { smoothWeight(abs(it - previous).toDouble(), smoothness) }
        return weightedPick(pool, weights, random)
    }

    /**
     * At smoothness 0 every pitch is equally likely. As smoothness rises, the
     * weight of a pitch falls off exponentially with its distance in semitones.
     */
    private fun smoothWeight(distanceSemitones: Double, smoothness: Float): Double =
        exp(-distanceSemitones * smoothness * 0.6)

    private fun weightedPick(items: List<Int>, weights: List<Double>, random: Random): Int {
        val total = weights.sum()
        if (total <= 0.0) return items.random(random)
        var r = random.nextDouble() * total
        for (i in items.indices) {
            r -= weights[i]
            if (r <= 0.0) return items[i]
        }
        return items.last()
    }

    private fun rollRhythm(count: Int, feel: RhythmFeel, random: Random): List<Double> =
        when (feel) {
            RhythmFeel.EVEN -> List(count) { 0.5 }
            RhythmFeel.MIXED -> List(count) { index ->
                if (index == count - 1) {
                    // Land the phrase on a longer note so it feels finished.
                    1.0
                } else {
                    // Eighths most of the time, with sixteenths and quarters mixed in.
                    when (random.nextInt(10)) {
                        0, 1 -> 0.25
                        8, 9 -> 1.0
                        else -> 0.5
                    }
                }
            }
        }
}
