package com.elendheim.melodymaker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elendheim.melodymaker.music.ChordType
import com.elendheim.melodymaker.music.MelodyEngine
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

class MelodyViewModel : ViewModel() {

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

    fun previewNote(midi: Int) {
        playJob?.cancel()
        playingIndex = -1
        viewModelScope.launch(Dispatchers.Default) { TonePlayer.playSingle(midi) }
    }

    fun melodyText(): String = melody.joinToString(", ") { it.name }

    override fun onCleared() {
        TonePlayer.stop()
    }
}
