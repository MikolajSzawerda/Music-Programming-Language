package com.declarative.music.parser.production.expression.relation;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record LessExpression(Expression left, Expression right) implements Expression {
    @Override
    public void accept(final Visitor visitor) {

    }
}