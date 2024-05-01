package com.declarative.music.parser.production.expression.pipe;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record PipeExpression(Expression left, InlineFuncCall right) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
