package com.declarative.music.interpreter.values.music;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Note
{
    private Pitch pitch;
    private int octave;
    private Rythm duration;
}
