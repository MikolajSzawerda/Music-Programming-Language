package com.declarative.music.parser.production.expression.lambda;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

import java.util.List;

public record LambdaCall(String name, List<Expression> arguments) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
