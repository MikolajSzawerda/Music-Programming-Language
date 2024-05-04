package com.declarative.music.midi;

public record Note(Pitch pitch, int octave, Duration duration, long startTick) {
}
