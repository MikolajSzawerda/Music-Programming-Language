package com.declarative.music.interpreter.type;

public class BooleanType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        return other.getClass() == BooleanType.class;
    }
}
