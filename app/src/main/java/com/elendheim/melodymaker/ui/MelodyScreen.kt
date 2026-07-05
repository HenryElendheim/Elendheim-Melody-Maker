package com.elendheim.melodymaker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elendheim.melodymaker.MelodyViewModel
import com.elendheim.melodymaker.music.ChordType
import com.elendheim.melodymaker.music.Note
import com.elendheim.melodymaker.music.RhythmFeel
import kotlinx.coroutines.launch

@Composable
fun MelodyScreen(viewModel: MelodyViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header()

            ResultCard(
                viewModel = viewModel,
                onCopy = {
                    clipboard.setText(AnnotatedString(viewModel.melodyText()))
                    scope.launch { snackbarHostState.showSnackbar("Copied: ${viewModel.melodyText()}") }
                }
            )

            Button(
                onClick = { viewModel.roll() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text("Roll", style = MaterialTheme.typography.titleLarge)
            }

            ConstraintsCard(viewModel)
            ChordLockCard(viewModel)

            Spacer(Modifier.height(8.dp))
        }
    }

    val editingIndex = viewModel.editingIndex
    if (editingIndex in viewModel.melody.indices) {
        NoteEditorDialog(
            initialMidi = viewModel.melody[editingIndex].midi,
            onPreview = { viewModel.previewNote(it) },
            onConfirm = { viewModel.applyEdit(it) },
            onDismiss = { viewModel.closeEdit() }
        )
    }
}

@Composable
private fun NoteEditorDialog(
    initialMidi: Int,
    onPreview: (Int) -> Unit,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var midi by remember(initialMidi) { mutableIntStateOf(initialMidi) }

    fun change(delta: Int) {
        val next = (midi + delta).coerceIn(Note.MIN_MIDI, Note.MAX_MIDI)
        if (next != midi) {
            midi = next
            onPreview(next)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit note") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    Note.name(midi),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepperButton("-", "Down one semitone") { change(-1) }
                    Text(
                        "Semitone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StepperButton("+", "Up one semitone") { change(1) }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepperButton("-", "Down one octave") { change(-12) }
                    Text(
                        "Octave",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StepperButton("+", "Up one octave") { change(12) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(midi) }) { Text("Set note") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun Header() {
    Column {
        Text(
            "Elendheim Music Maker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Random phrases inside your constraints. Roll until one catches your ear.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(viewModel: MelodyViewModel, onCopy: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (viewModel.melody.isEmpty()) {
                Text(
                    "Hit Roll to generate a phrase. Tap any note to hear it and change it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.melody.forEachIndexed { index, event ->
                        NoteChip(
                            label = event.name,
                            lengthLabel = event.lengthLabel,
                            playing = viewModel.playingIndex == index,
                            onClick = { viewModel.startEdit(index) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.play() }) { Text("Play") }
                    OutlinedButton(onClick = { viewModel.mutate() }) { Text("Mutate") }
                    OutlinedButton(onClick = onCopy) { Text("Copy") }
                }
            }
        }
    }
}

@Composable
private fun NoteChip(
    label: String,
    lengthLabel: String,
    playing: Boolean,
    onClick: () -> Unit
) {
    val bg = if (playing) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (playing) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable(onClickLabel = "Edit note $label", onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
            Text(
                lengthLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (playing) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConstraintsCard(viewModel: MelodyViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Constraints", style = MaterialTheme.typography.titleMedium)

            RangeStepper(
                label = "Low note",
                value = Note.name(viewModel.lowMidi),
                onDown = { viewModel.nudgeLow(-1) },
                onUp = { viewModel.nudgeLow(1) }
            )
            RangeStepper(
                label = "High note",
                value = Note.name(viewModel.highMidi),
                onDown = { viewModel.nudgeHigh(-1) },
                onUp = { viewModel.nudgeHigh(1) }
            )

            LabeledSlider(
                label = "Notes: ${viewModel.noteCount}",
                value = viewModel.noteCount.toFloat(),
                onValueChange = { viewModel.noteCount = it.toInt() },
                valueRange = 4f..8f,
                steps = 3
            )

            RhythmPicker(viewModel)

            Column {
                LabeledSlider(
                    label = "Smoothness",
                    value = viewModel.smoothness,
                    onValueChange = { viewModel.smoothness = it },
                    valueRange = 0f..1f,
                    steps = 0
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Anything goes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Stepwise",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LabeledSlider(
                label = "Tempo: ${viewModel.bpm} BPM",
                value = viewModel.bpm.toFloat(),
                onValueChange = { viewModel.bpm = it.toInt() },
                valueRange = 60f..160f,
                steps = 0
            )
        }
    }
}

@Composable
private fun RangeStepper(label: String, value: String, onDown: () -> Unit, onUp: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        StepperButton("-", "Lower $label by one semitone", onDown)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(64.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        StepperButton("+", "Raise $label by one semitone", onUp)
    }
}

@Composable
private fun StepperButton(symbol: String, description: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
        modifier = Modifier
            .size(44.dp)
            .clickable(onClickLabel = description, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                symbol,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
private fun RhythmPicker(viewModel: MelodyViewModel) {
    Column {
        Text("Rhythm", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            RhythmFeel.entries.forEachIndexed { index, feel ->
                SegmentedButton(
                    selected = viewModel.rhythm == feel,
                    onClick = { viewModel.rhythm = feel },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = RhythmFeel.entries.size
                    )
                ) {
                    Text(feel.label)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChordLockCard(viewModel: MelodyViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chord lock", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Only roll notes from a chord you are working over.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = viewModel.chordLock,
                    onCheckedChange = { viewModel.chordLock = it }
                )
            }

            if (viewModel.chordLock) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownField(
                        label = "Root",
                        value = Note.NAMES[viewModel.chordRoot],
                        options = Note.NAMES.toList(),
                        onSelected = { viewModel.chordRoot = Note.NAMES.indexOf(it) },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = "Chord",
                        value = viewModel.chordType.label,
                        options = ChordType.entries.map { it.label },
                        onSelected = { label ->
                            viewModel.chordType = ChordType.entries.first { it.label == label }
                        },
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
