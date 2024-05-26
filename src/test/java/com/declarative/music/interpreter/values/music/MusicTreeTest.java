package com.declarative.music.interpreter.values.music;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class MusicTreeTest
{
    private MusicTree tested;

    @BeforeEach
    void init()
    {
        tested = new MusicTree();
    }

    @Test
    void shouldModelSeqenceOfNotes()
    {
        var melody = List.of(
            new Note(Pitch.C, 4, Rythm.e),
            new Note(Pitch.E, 4, Rythm.q),
            new Note(Pitch.G, 4, Rythm.q)
        );
    }
}