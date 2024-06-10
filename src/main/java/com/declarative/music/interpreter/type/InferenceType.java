package com.declarative.music.interpreter.type;

public class InferenceType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        return true;
    }
}
