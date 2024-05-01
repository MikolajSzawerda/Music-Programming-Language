package com.declarative.music.parser.production.expression;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.type.Type;

public record CastExpresion(Expression value, Type type) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
