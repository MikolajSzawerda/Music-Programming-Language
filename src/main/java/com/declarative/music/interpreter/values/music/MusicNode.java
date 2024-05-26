package com.declarative.music.interpreter.values.music;

import java.util.List;

import lombok.Data;


@Data
public class MusicNode
{
    private Note note;
    private List<MusicNode> nexts;

    public MusicNode(Note note)
    {
        this.note = note;
    }

    public MusicNode addNote(Note note)
    {
        var node = new MusicNode(note);
        nexts.add(node);
        return node;
    }
}
