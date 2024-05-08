package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Interpretable;
import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;

import java.util.List;

public record Program(List<Statement> statements) implements Interpretable {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Position position() {
        return new Position(0, 0);
    }
}
