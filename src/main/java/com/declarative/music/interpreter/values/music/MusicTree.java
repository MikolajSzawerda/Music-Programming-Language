package com.declarative.music.interpreter.values.music;

import java.util.List;

import lombok.Data;


@Data
public class MusicTree
{
    private List<MusicNode> parraler;

    public void addSequence(List<Note> sequence)
    {
        if (sequence.isEmpty())
        {
            return;
        }
        MusicNode current = new MusicNode(sequence.removeFirst());
        for (var note : sequence)
        {
            current = current.addNote(note);
        }
        parraler.add(current);
    }
}
