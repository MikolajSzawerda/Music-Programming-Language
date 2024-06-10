package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.NoteModifier;


public interface Modifable {
    Modifable accept(NoteModifier modifier);
}
