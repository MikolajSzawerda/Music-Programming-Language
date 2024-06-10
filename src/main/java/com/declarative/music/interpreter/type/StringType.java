package com.declarative.music.interpreter.type;

public class StringType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        return other.getClass() == StringType.class;
    }
}
