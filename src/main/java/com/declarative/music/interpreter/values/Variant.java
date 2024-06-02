package com.declarative.music.interpreter.values;

import lombok.Getter;


public record Variant<T>(T value, @Getter Class<T> type)
{

    @Override
    public T value()
    {
        if (value instanceof VariableReference)
        {
            return ((VariableReference<T>) value).getValue();
        }
        return value;
    }

    public VariableReference<T> getReference()
    {
        if (value instanceof VariableReference)
        {
            return (VariableReference<T>) value;
        }
        throw new IllegalStateException("Value is not a reference");
    }

    public <R> R castTo(Class<R> targetType)
    {
        if (value instanceof VariableReference && targetType.isInstance(value()))
        {
            return targetType.cast(value());
        }
        if (targetType.isInstance(value))
        {
            return targetType.cast(value);
        }
        throw new IllegalStateException("Value is not of type: " + targetType.getName());
    }

    public Class<?> valueType()
    {
        if (value instanceof VariableReference)
        {
            return ((VariableReference<T>) value).getValue().getClass();
        }
        return value.getClass();
    }

    @Override
    public String toString()
    {
        return value.toString();
    }
}

