package com.declarative.music.interpreter.values;

import com.declarative.music.interpreter.LambdaClousure;


public class LambdaReference implements Reference
{
    private LambdaClousure expression;

    public LambdaReference(final LambdaClousure expression)
    {
        this.expression = expression;
    }

    @Override
    public Object getValue()
    {
        return expression;
    }

    @Override
    public void setValue(final Object value)
    {
        this.expression = (LambdaClousure) value;
    }
}
