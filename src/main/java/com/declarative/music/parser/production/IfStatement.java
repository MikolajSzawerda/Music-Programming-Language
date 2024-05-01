package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record IfStatement(Expression condition, Block instructions, IfStatement otherwise) implements Statement {
    public IfStatement(final Expression condition, final Block instructions) {
        this(condition, instructions, null);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
