package com.declarative.music.interpreter.values;

import java.util.Objects;

import lombok.Getter;


public final class Variant<T>
{
    private final T value;
    @Getter
    private final Class<T> type;

    public Variant(T value, Class<T> type)
    {
        this.value = value;
        this.type = type;
    }

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

    public Class<T> type()
    {
        return type;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }
        var that = (Variant) obj;
        return Objects.equals(this.value, that.value) &&
            Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value, type);
    }

}

