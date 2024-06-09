package com.declarative.music.interpreter.type;

public class TemplateType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        if (other instanceof IntType) {
            return true;
        }
        return other.getClass() == TemplateType.class;
    }
}
