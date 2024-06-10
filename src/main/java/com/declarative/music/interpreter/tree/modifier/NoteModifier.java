package com.declarative.music.interpreter.tree.modifier;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import lombok.Builder;
import lombok.Data;


@Builder(setterPrefix = "with")
@Data
public class NoteModifier implements ModifierVisitor<Note> {
    private Pitch pitch;
    private Rythm rythm;
    private Integer octave;

    @Override
    public SimpleNode<Note> visit(SimpleNode<Note> node) {
        var note = node.getValue();
        var newNote = new Note(
                note.getPitch() == null ? pitch : note.getPitch(),
                note.getOctave() == null ? octave : note.getOctave(),
                note.getDuration() == null ? rythm : note.getDuration()
        );
        return new SimpleNode<>(newNote);
    }

    @Override
    public GroupNode<Note> visit(GroupNode<Note> node) {
        return null;
    }

    @Override
    public SequenceNode<Note> visit(SequenceNode<Note> node) {
        return null;
    }

}
