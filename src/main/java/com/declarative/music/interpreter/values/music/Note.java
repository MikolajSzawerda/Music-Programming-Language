package com.declarative.music.interpreter.values.music;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Note implements MusicNode
{
    private Pitch pitch;
    private int octave;
    private Rythm duration;

    @Override
    public MusicNode add(final MusicNode node)
    {
        throw new UnsupportedOperationException("Note object doesn't contain other notes");
    }

    @Override
    public List<MusicNode> getChildren()
    {
        return new ArrayList<>();
    }

    @Override
    public void collectNotes(final List<Note> notes)
    {
        notes.add(this);
    }
}
