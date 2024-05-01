package com.declarative.music.parser.production.expression;

import com.declarative.music.interpreter.Visitor;

public record VariableReference(String name) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
