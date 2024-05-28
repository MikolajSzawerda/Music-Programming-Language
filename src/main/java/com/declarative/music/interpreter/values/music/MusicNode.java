package com.declarative.music.interpreter.values.music;

import java.util.List;


public interface MusicNode
{
    void add(MusicNode node);

    List<MusicNode> getChildren();

    void collectNotes(List<Note> notes);
}
