package com.declarative.music.interpreter.values.music;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.NodeFactory;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;


public class MusicFactory implements NodeFactory<Note>
{
    @Override
    public GroupNode<Note> createGroup()
    {
        return new Chord();
    }

    @Override
    public SequenceNode<Note> createSequence()
    {
        return new Phrase();
    }

    @Override
    public SimpleNode<Note> createSimpleNode(Note note)
    {
        return new NoteNode(note);
    }
}
