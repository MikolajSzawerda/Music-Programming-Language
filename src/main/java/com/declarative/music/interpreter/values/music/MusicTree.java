package com.declarative.music.interpreter.values.music;

import java.util.List;

import lombok.Data;


@Data
public class MusicTree
{
    private final Chord root = new Chord();

    public void addSequence(List<Note> notes)
    {
        var current = root;
        for (var note : notes)
        {
            var newNode = new Chord();
            newNode.add(note);
            current.add(newNode);
            current = newNode;
        }
    }
}
