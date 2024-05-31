package com.declarative.music.parser.production.expression;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;

public record VariableReference(String name, Position position) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
