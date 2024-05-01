package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Interpretable;
import com.declarative.music.interpreter.Visitor;

import java.util.List;

public record Block(List<Statement> statements) implements Interpretable {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
