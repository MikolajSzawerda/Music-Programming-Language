package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Interpretable;
import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;

import java.util.List;

public record Block(List<Statement> statements, Position position) implements Interpretable {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
