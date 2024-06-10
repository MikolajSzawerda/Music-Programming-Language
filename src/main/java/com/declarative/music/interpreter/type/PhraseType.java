package com.declarative.music.interpreter.type;

public class PhraseType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        if (other instanceof NoteType) {
            return true;
        }
        return other.getClass() == PhraseType.class;
    }
}
