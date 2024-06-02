package com.declarative.music.interpreter.values.music;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class Chord implements MusicNode
{
    private final List<MusicNode> nodes;

    public Chord()
    {
        nodes = new ArrayList<>();
    }

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

    }
}
