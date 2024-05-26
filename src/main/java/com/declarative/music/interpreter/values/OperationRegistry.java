package com.declarative.music.interpreter.values;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;


public class OperationRegistry
{
    private final Map<String, BiFunction<Variant<?>, Variant<?>, Variant<?>>> registry = new HashMap<>();

    public <T, U, R> OperationRegistry register(Class<T> leftType, Class<U> rightType, BiFunction<T, U, R> operation, Class<R> returnType)
    {
        String key = generateKey(leftType, rightType);
        registry.put(key, (l, r) -> new Variant<>(operation.apply((T) l.value(), (U) r.value()), returnType));
        return this;
    }

    public Variant<?> apply(String operation, Variant<?> left, Variant<?> right)
    {
        String key = generateKey(left.valueType(), right.valueType());
        BiFunction<Variant<?>, Variant<?>, Variant<?>> operationFunc = registry.get(key);
        if (operationFunc != null)
        {
            return operationFunc.apply(left, right);
        }
        throw new IllegalStateException("INTERPRETATION ERROR Unsupported types for operation: " + operation);
    }

    private String generateKey(Class<?> leftType, Class<?> rightType)
    {
        return leftType.getName() + "," + rightType.getName();
    }
}
