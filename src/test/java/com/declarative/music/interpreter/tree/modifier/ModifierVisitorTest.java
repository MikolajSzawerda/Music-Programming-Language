package com.declarative.music.interpreter.tree.modifier;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.music.MusicTree;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class ModifierVisitorTest {
    @Test
    void shouldApplyModification() {
        // given
        var modifier = new NoteModifier(Pitch.C, Rythm.q, 4);
        var tree = new SimpleNode<Note>(new Note(Pitch.E, 4, null));
        tree.setModifier(modifier);

        // when
        var result = tree.getModified();

        // then
        Assertions.assertEquals(new Note(Pitch.E, 4, Rythm.q), result.getValue());
    }

    @Test
    void shouldApplyModificationToWholeTree() {
        var modifier = new NoteModifier(Pitch.C, Rythm.q, 4);
        var tree = new SequenceNode<Note>(List.of(
                new GroupNode<Note>(List.of(
                        new SimpleNode<Note>(new Note(Pitch.E, 4, null)),
                        new SimpleNode<Note>(new Note(Pitch.E, 4, null)
                        ),
                        new SimpleNode<Note>(new Note(Pitch.E, 4, null))
                ))));
        var expectedTree = new SequenceNode<Note>(List.of(
                new GroupNode<Note>(List.of(
                        new SimpleNode<Note>(new Note(Pitch.E, 4, Rythm.q)),
                        new SimpleNode<Note>(new Note(Pitch.E, 4, Rythm.q)
                        ),
                        new SimpleNode<Note>(new Note(Pitch.E, 4, Rythm.q))
                ))));
        tree.setModifier(modifier);

        var result = tree.getModified();

        assertThat(result)
                .isEqualToComparingFieldByFieldRecursively(expectedTree);
    }

    @Test
    void shouldModifyMusicTree() {
        var modifier = new NoteModifier(Pitch.C, Rythm.q, 4);
        var tree = new MusicTree()
                .appendToGroup(new Note(Pitch.E, 4, null))
                .appendToSequence(new Note(Pitch.E, 4, null));
        var expectedTree = new MusicTree()
                .appendToGroup(new Note(Pitch.E, 4, Rythm.q))
                .appendToSequence(new Note(Pitch.E, 4, Rythm.q));
        tree.setModifier(modifier);
        var result = tree.getModified();
        assertThat(result.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedTree.getRoot());
    }
}