package com.declarative.music.parser.production.expression.array;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.VariableReference;

public record ListComprehension(Expression mapper, VariableReference tempName,
                                Expression iterable) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
