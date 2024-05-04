package com.declarative.music.midi;

public enum Pitch {
    C,
    C_SHARP,
    D,
    D_SHARP,
    E,
    F,
    F_SHARP,
    G,
    G_SHARP;

    public static Pitch from(final String pitch) {
        if (pitch.equals("C")) {
            return C;
        }
        if (pitch.equals("C#")) {
            return C_SHARP;
        }
        if (pitch.equals("D")) {
            return D;
        }
        if (pitch.equals("D#")) {
            return D_SHARP;
        }
        if (pitch.equals("E")) {
            return E;
        }
        if (pitch.equals("F")) {
            return F;
        }
        if (pitch.equals("F#")) {
            return F_SHARP;
        }
        if (pitch.equals("G")) {
            return G;
        }
        if (pitch.equals("G#")) {
            return G_SHARP;
        }
        throw new IllegalArgumentException();
    }
}
