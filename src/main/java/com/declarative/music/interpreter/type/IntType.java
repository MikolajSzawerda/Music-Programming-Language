package com.declarative.music.interpreter.type;

public class IntType implements TypeCheck {
    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        if (other instanceof TemplateType) {
            return true;
        }
        return other.getClass() == IntType.class;
    }

}
