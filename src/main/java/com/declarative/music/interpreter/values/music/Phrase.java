package com.declarative.music.interpreter.values.music;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class Phrase implements MusicNode
{
    private final List<MusicNode> nodes;

    public Phrase()
    {
        nodes = new ArrayList<>();
    }

    @Override
    public MusicNode add(final MusicNode node)
    {
        nodes.addAll(node.getChildren());
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
