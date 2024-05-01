package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record AssigmentStatement(String identifier, Expression value) implements Statement {

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
