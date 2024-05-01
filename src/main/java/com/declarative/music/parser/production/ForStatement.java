package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record ForStatement(Declaration declaration, Expression iterable, Block instructions) implements Statement {
    @Override
    public void accept(final Visitor visitor) {

    }
}
