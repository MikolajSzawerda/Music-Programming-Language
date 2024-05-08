package com.declarative.music.parser.production.expression;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.type.Type;

public record CastExpresion(Expression value, Type type, Position position) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
