package com.declarative.music.interpreter.values.music;

import lombok.Builder;
import lombok.Data;


@Builder(setterPrefix = "with")
@Data
public class Modifier
{
    private Pitch pitch;
    private Rythm rythm;
    private int octave;
}
