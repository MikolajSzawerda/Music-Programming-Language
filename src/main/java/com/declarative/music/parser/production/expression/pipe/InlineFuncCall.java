package com.declarative.music.parser.production.expression.pipe;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

import java.util.List;

public record InlineFuncCall(String name, List<Expression> arguments) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
