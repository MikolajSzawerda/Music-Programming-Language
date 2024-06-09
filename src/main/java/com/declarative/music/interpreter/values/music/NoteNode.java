package com.declarative.music.interpreter.values.music;

import com.declarative.music.interpreter.tree.SimpleNode;

import java.util.Optional;


public class NoteNode extends SimpleNode<Note> {
    public NoteNode(Note noteN) {
        super(noteN);
    }

    public Note getValue() {
        if (modifier == null) {
            return this.value;
        }
        return new Note(
                Optional.ofNullable(this.value.getPitch()).orElse(modifier.getPitch()),
                Optional.ofNullable(this.value.getOctave()).orElse(modifier.getOctave()),
                Optional.ofNullable(this.value.getDuration()).orElse(modifier.getRythm())
        );
    }
}
