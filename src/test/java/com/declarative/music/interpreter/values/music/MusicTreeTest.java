package com.declarative.music.interpreter.values.music;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    void shouldRepresentNoteSequence()
    {
        // given
        var sequence = List.of(
            new Note(Pitch.C, 4, Rythm.q),
            new Note(Pitch.E, 4, Rythm.q),
            new Note(Pitch.G, 4, Rythm.q)
        );
        tested.addSequence(sequence);
        var result = tested.getRoot();
        var chord = new StrangeChord(new Note(Pitch.C, 4, Rythm.q))
            .add(
                new StrangeChord(new Note(Pitch.E, 4, Rythm.q))
                    .add(new StrangeChord(new Note(Pitch.G, 4, Rythm.q)))
            );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(chord);
    }

    private StrangeChord createNoteSequenceTree(List<Note> notes)
    {
        StrangeChord current = null;
        for (var note : notes.reversed())
        {
            var newChord = new StrangeChord(note);
            if (current != null)
            {
                newChord.add(current);
            }
            current = newChord;
        }
        return current;
    }
}