package com.declarative.music.interpreter.values.music;

import java.util.ArrayList;
import java.util.List;


public class Chord implements MusicNode
{
    private final List<MusicNode> nodes = new ArrayList<>();

    @Override
    public void add(final MusicNode node)
    {
        nodes.add(node);
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
