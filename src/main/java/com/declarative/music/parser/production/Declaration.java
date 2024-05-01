package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.type.Type;

public record Declaration(Type type, String name, Expression value) implements Statement {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
