package com.declarative.music.interpreter.values;

public class StringReference implements Reference
{
    private String value;

    public StringReference(String name, String value)
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
        this.value = (String) value;
    }
}
