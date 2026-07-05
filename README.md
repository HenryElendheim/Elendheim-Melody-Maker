# Elendheim Melody Maker

An Android app for breaking out of your own melodic habits. Set a few constraints, hit Roll, and the app generates a short random phrase, plays it back, and shows the note names so you can punch the good ones straight into FL Studio's piano roll.

Most rolls sound like nothing. That is the point: rolling takes one second, and every so often randomness produces a contour your hands would never have played.

## Features

- Note range: pick a low and high note (C2 to C7, default C4 to C5)
- Note count: 4 to 8 notes per phrase
- Rhythm feel: all even lengths, or mixed lengths that end on a longer note
- Smoothness dial: weight the roll toward stepwise motion so phrases sound melodic instead of random bleeps
- Chord lock: restrict the roll to the tones of a chord you are working over (root plus major, minor, major 7, minor 7, dominant 7, or sus4)
- Mutate: keep the roll you liked but change exactly one note. Roll, select, mutate: melodies you breed rather than roll
- Tap any note chip to hear it alone
- Copy: puts the phrase on the clipboard as note names, for example "E4, G4, A4, E4, D4"
- Built-in synth playback, no soundfonts or permissions needed
- Dark mode only, by design

## Getting the APK

Every push builds a debug APK in GitHub Actions. Open the Actions tab, pick the latest Build run, and download the "melody-maker-debug-apk" artifact. Enable "install from unknown sources" on your phone and install it.

## Building locally

Requires JDK 17 and the Android SDK (compile SDK 35).

```
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

## Tech

Kotlin, Jetpack Compose, Material 3. Audio is synthesized on the fly with AudioTrack (a soft pluck: sine plus two decaying harmonics). The generator core in `app/src/main/java/com/elendheim/melodymaker/music/` is pure Kotlin and covered by unit tests.
