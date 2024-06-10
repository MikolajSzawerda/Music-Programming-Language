package com.declarative.music.interpreter.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ArrayType implements TypeCheck {
    private TypeCheck type;

    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        if (other instanceof ArrayType otherArray) {
            return type.isCompatible(otherArray.type);
        }
        return false;
    }
}
