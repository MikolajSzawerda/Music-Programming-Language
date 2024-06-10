package com.declarative.music.interpreter.values.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Song {
    private MusicTree song;
    private int BPM;
    private String instrument;
}
