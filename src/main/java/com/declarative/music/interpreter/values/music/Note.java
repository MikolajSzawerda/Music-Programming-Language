package com.declarative.music.interpreter.values.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class Note
{
    private Pitch pitch;
    private Integer octave;
    private Rythm duration;
}
