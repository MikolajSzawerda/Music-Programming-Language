package com.declarative.music.interpreter.values;

public class IntReference implements Reference
{
    private int value;

    public IntReference(String name, int value)
    {
        this.value = value;
    }

    public IntReference(int value)
    {
        this.value = value;
    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public void setValue(final Object value)
    {
        this.value = (int) value;
    }
}
