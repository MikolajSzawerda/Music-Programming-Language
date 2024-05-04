package com.declarative.music.parser.production.expression.array;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

import java.util.List;

public record ArrayExpression(List<Expression> items) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}