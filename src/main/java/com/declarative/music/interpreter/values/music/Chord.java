package com.declarative.music.interpreter.values.music;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class Chord implements MusicNode
{
    private MusicNode note;

    public Chord(final MusicNode note)
    {
        this.note = note;
    }

    private final List<MusicNode> nodes = new ArrayList<>();

    @Override
    public MusicNode add(final MusicNode node)
    {
        nodes.add(node);
        return this;
    }

    @Override
    public List<MusicNode> getChildren()
    {
        return nodes;
    }

    @Override
    public void collectNotes(final List<Note> notes)
    {
        notes.forEach(node -> node.collectNotes(notes));
    }
}
