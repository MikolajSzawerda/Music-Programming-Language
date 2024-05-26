package com.declarative.music.parser.production.type;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;

public record InferenceType(Position position) implements Type {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}