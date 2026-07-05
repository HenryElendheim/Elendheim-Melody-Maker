package com.elendheim.melodymaker.music

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders a melody to PCM with a soft pluck-style synth and plays it through
 * a static AudioTrack. No permissions or audio assets required.
 */
object TonePlayer {

    private const val SAMPLE_RATE = 44100
    private var track: AudioTrack? = null

    fun beatsToMillis(beats: Double, bpm: Int): Long = (beats * 60_000.0 / bpm).toLong()

    @Synchronized
    fun play(events: List<NoteEvent>, bpm: Int) {
        if (events.isEmpty()) return
        playPcm(render(events, bpm))
    }

    @Synchronized
    fun playSingle(midi: Int) {
        playPcm(render(listOf(NoteEvent(midi, 0.5)), 100))
    }

    @Synchronized
    fun stop() {
        track?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        track = null
    }

    private fun playPcm(pcm: ShortArray) {
        stop()
        val bytes = pcm.size * 2
        val newTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(bytes)
            .build()
        newTrack.write(pcm, 0, pcm.size)
        newTrack.play()
        track = newTrack
    }

    /**
     * Additive render: each note gets an attack-plus-exponential-decay envelope
     * and is allowed to ring slightly past its slot so notes blend smoothly.
     */
    fun render(events: List<NoteEvent>, bpm: Int): ShortArray {
        val slotSamples = events.map { (beatsToMillis(it.beats, bpm) * SAMPLE_RATE / 1000).toInt() }
        val tailSamples = SAMPLE_RATE / 2
        val total = slotSamples.sum() + tailSamples
        val mix = FloatArray(total)

        var cursor = 0
        for ((index, event) in events.withIndex()) {
            val slot = slotSamples[index]
            val ring = min(slot + SAMPLE_RATE / 3, total - cursor)
            val freq = Note.frequency(event.midi)
            val attackSamples = (SAMPLE_RATE * 0.006).toInt().coerceAtLeast(1)
            val decayRate = 4.0 / slot.coerceAtLeast(1)

            for (i in 0 until ring) {
                val t = i.toDouble() / SAMPLE_RATE
                val attack = min(1.0, i.toDouble() / attackSamples)
                val decay = exp(-decayRate * i)
                val env = attack * decay
                val phase = 2.0 * PI * freq * t
                val sample = sin(phase) +
                    0.35 * sin(2.0 * phase) * decay +
                    0.12 * sin(3.0 * phase) * decay * decay
                mix[cursor + i] += (sample * env * 0.4).toFloat()
            }
            cursor += slot
        }

        // Normalize only if overlapping tails pushed the mix past full scale.
        var peak = 0f
        for (s in mix) {
            val a = if (s < 0) -s else s
            if (a > peak) peak = a
        }
        val gain = if (peak > 0.95f) 0.95f / peak else 1f

        return ShortArray(total) { i ->
            (mix[i] * gain * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
    }
}
