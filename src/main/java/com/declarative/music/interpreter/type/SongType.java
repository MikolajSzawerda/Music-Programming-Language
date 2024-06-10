package com.declarative.music.interpreter.type;

public class SongType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        return other.getClass() == SongType.class;
    }
}
