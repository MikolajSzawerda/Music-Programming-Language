package com.declarative.music.interpreter.values.music;

import com.declarative.music.interpreter.tree.Modifable;

import lombok.Builder;
import lombok.Data;


@Builder(setterPrefix = "with")
@Data
public class NoteModifier
{
    private Pitch pitch;
    private Rythm rythm;
    private int octave;

    public Modifable apply(Modifable obj)
    {
        return obj;
    }
}
