package com.declarative.music.midi;

import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;


public record MidiNote(Pitch pitch, int octave, Rythm duration, long startTick)
{
    public static MidiNote from(Note note)
    {
        return new MidiNote(
            note.getPitch(),
            note.getOctave(),
            note.getDuration(),
            0
        );
    }
}
