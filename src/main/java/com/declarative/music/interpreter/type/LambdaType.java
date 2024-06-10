package com.declarative.music.interpreter.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.IntStream;

@AllArgsConstructor
@Getter
public class LambdaType implements TypeCheck {
    private final List<TypeCheck> parameters;
    private final TypeCheck returnType;

    @Override
    public boolean isCompatible(TypeCheck other) {
        if (other instanceof InferenceType) {
            return true;
        }
        if (other instanceof LambdaType otherLambda) {
            var paramsCorrect = IntStream.range(0, parameters.size())
                    .allMatch(idx -> parameters.get(idx).isCompatible(otherLambda.parameters.get(idx)));
            return paramsCorrect && returnType.isCompatible(otherLambda.returnType);
        }
        return false;
    }
}
