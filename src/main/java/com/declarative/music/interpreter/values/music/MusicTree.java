package com.declarative.music.interpreter.values.music;

import java.util.List;

import lombok.Data;


@Data
public class MusicTree
{
    private StrangeChord root = null;

    public void addSequence(List<Note> notes)
    {
        StrangeChord current = root;
        for (var note : notes.reversed())
        {
            var newChord = new StrangeChord(note);
            if (current != null)
            {
                newChord.add(current);
            }
            current = newChord;
        }
        root = current;
    }
}
