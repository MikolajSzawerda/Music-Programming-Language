package com.declarative.music.parser.production.expression.modifier;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record ModifierExpression(Expression modified, Modifier modifier) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
