package com.declarative.music.parser.production.expression.lambda;

import java.util.List;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;


public record FunctionCall(String name, List<Expression> arguments) implements ExecutionCall
{
    @Override
    public void accept(final Visitor visitor)
    {
        visitor.visit(this);
    }
}
