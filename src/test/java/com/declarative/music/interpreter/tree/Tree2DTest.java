package com.declarative.music.interpreter.tree;

import org.junit.jupiter.api.Test;

import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.NoteNode;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;


class Tree2DTest
{
    @Test
    void shouldCreateProperStructure()
    {
        var a = new Variant<>(10, Integer.class);
        var b = new SequenceNode<Integer>();
        var c = new GroupNode<Integer>();
//        c.accept(new Variant<>(10, Integer.class));
//        b.accept(a);
        b.accept(c);
    }

    @Test
    void shouldCreateProperStructure_WithMusic()
    {
        var a = new Variant<>(new Note(Pitch.C, 4, Rythm.q), Note.class);
        var b = new SequenceNode<NoteNode>();
        var c = new GroupNode<NoteNode>();
//        c.accept(new Variant<>(new NoteNode(Pitch.C, 4, Rythm.q), NoteNode.class));
//        b.accept(a);
        b.accept(c);
    }

}