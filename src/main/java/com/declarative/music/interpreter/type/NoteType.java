package com.declarative.music.interpreter.type;

public class NoteType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        if (other instanceof PhraseType) {
            return true;
        }
        return other.getClass() == NoteType.class;
    }
}
