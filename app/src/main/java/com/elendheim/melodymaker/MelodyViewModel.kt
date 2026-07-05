package com.elendheim.melodymaker

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elendheim.melodymaker.music.ChordType
import com.elendheim.melodymaker.music.MelodyEngine
import com.elendheim.melodymaker.music.MidiWriter
import com.elendheim.melodymaker.music.Note
import com.elendheim.melodymaker.music.NoteEvent
import com.elendheim.melodymaker.music.RhythmFeel
import com.elendheim.melodymaker.music.RollSettings
import com.elendheim.melodymaker.music.TonePlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MelodyViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Constraints
    var lowMidi by mutableIntStateOf(60) // C4
        private set
    var highMidi by mutableIntStateOf(72) // C5
        private set
    var noteCount by mutableIntStateOf(6)
    var rhythm by mutableStateOf(RhythmFeel.EVEN)
    var smoothness by mutableFloatStateOf(0.5f)
    var bpm by mutableIntStateOf(100)

    // Chord lock
    var chordLock by mutableStateOf(false)
    var chordRoot by mutableIntStateOf(0) // pitch class, 0 = C
    var chordType by mutableStateOf(ChordType.MAJOR)

    // Result
    var melody by mutableStateOf<List<NoteEvent>>(emptyList())
        private set
    var playingIndex by mutableIntStateOf(-1)
        private set

    /** Index of the note being edited in the note editor, or -1 when closed. */
    var editingIndex by mutableIntStateOf(-1)
        private set

    // Settings (persisted)
    var showSettings by mutableStateOf(false)
    var largeText by mutableStateOf(prefs.getBoolean("large_text", false))
        private set
    var highContrast by mutableStateOf(prefs.getBoolean("high_contrast", false))
        private set
    var bigButtons by mutableStateOf(prefs.getBoolean("big_buttons", false))
        private set
    var hapticFeedback by mutableStateOf(prefs.getBoolean("haptic_feedback", true))
        private set
    var useFlats by mutableStateOf(prefs.getBoolean("use_flats", false))
        private set

    fun updateLargeText(value: Boolean) {
        largeText = value
        prefs.edit().putBoolean("large_text", value).apply()
    }

    fun updateHighContrast(value: Boolean) {
        highContrast = value
        prefs.edit().putBoolean("high_contrast", value).apply()
    }

    fun updateBigButtons(value: Boolean) {
        bigButtons = value
        prefs.edit().putBoolean("big_buttons", value).apply()
    }

    fun updateHapticFeedback(value: Boolean) {
        hapticFeedback = value
        prefs.edit().putBoolean("haptic_feedback", value).apply()
    }

    fun updateUseFlats(value: Boolean) {
        useFlats = value
        prefs.edit().putBoolean("use_flats", value).apply()
    }

    private var playJob: Job? = null

    private fun settings() = RollSettings(
        lowMidi = lowMidi,
        highMidi = highMidi,
        noteCount = noteCount,
        rhythm = rhythm,
        smoothness = smoothness,
        chordLock = chordLock,
        chordRootPitchClass = chordRoot,
        chordType = chordType
    )

    // Keep at least a fourth between low and high so rolls stay interesting.
    private val minSpan = 5

    fun nudgeLow(delta: Int) {
        lowMidi = (lowMidi + delta).coerceIn(Note.MIN_MIDI, highMidi - minSpan)
    }

    fun nudgeHigh(delta: Int) {
        highMidi = (highMidi + delta).coerceIn(lowMidi + minSpan, Note.MAX_MIDI)
    }

    fun roll() {
        melody = MelodyEngine.generate(settings())
        play()
    }

    fun mutate() {
        if (melody.isEmpty()) return
        melody = MelodyEngine.mutate(melody, settings())
        play()
    }

    fun play() {
        val events = melody
        if (events.isEmpty()) return
        playJob?.cancel()
        playJob = viewModelScope.launch {
            withContext(Dispatchers.Default) { TonePlayer.play(events, bpm) }
            try {
                for (i in events.indices) {
                    playingIndex = i
                    delay(TonePlayer.beatsToMillis(events[i].beats, bpm))
                }
            } finally {
                playingIndex = -1
            }
        }
    }

    fun startEdit(index: Int) {
        if (index in melody.indices) {
            editingIndex = index
            previewNote(melody[index].midi)
        }
    }

    fun closeEdit() {
        editingIndex = -1
    }

    /** Replaces the pitch of the note being edited, keeps its length, and replays. */
    fun applyEdit(midi: Int) {
        val index = editingIndex
        editingIndex = -1
        if (index !in melody.indices) return
        melody = melody.toMutableList().also {
            it[index] = it[index].copy(midi = midi.coerceIn(Note.MIN_MIDI, Note.MAX_MIDI))
        }
        play()
    }

    fun previewNote(midi: Int) {
        playJob?.cancel()
        playingIndex = -1
        viewModelScope.launch(Dispatchers.Default) { TonePlayer.playSingle(midi) }
    }

    fun noteName(midi: Int): String = Note.name(midi, useFlats)

    fun melodyText(): String = melody.joinToString(", ") { noteName(it.midi) }

    /** The current melody as a Standard MIDI File, ready to save. */
    fun midiBytes(): ByteArray = MidiWriter.write(melody, bpm)

    override fun onCleared() {
        TonePlayer.stop()
    }
}
