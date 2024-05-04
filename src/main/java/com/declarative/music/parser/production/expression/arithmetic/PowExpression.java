package com.declarative.music.parser.production.expression.arithmetic;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record PowExpression(Expression left, Expression right) implements Expression {
    @Override
    public void accept(final Visitor visitor) {

    }
}
