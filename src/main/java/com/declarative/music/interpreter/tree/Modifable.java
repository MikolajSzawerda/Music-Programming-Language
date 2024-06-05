package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.values.music.NoteModifier;


public interface Modifable
{
    Modifable accept(NoteModifier modifier);
}
