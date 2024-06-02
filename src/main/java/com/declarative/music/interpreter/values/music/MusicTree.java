package com.declarative.music.interpreter.values.music;

import java.util.List;

import lombok.Data;


@Data
public class MusicTree
{
    private Chord root = null;

    public void addSequence(List<Note> notes)
    {
        Chord current = root;
        for (var note : notes.reversed())
        {
            var newChord = new Chord(note);
            if (current != null)
            {
                newChord.add(current);
            }
            current = newChord;
        }
        root = current;
    }
}
