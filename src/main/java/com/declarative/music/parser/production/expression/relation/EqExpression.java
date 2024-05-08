package com.declarative.music.parser.production.expression.relation;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.expression.Expression;

public record EqExpression(Expression left, Expression right) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Position position() {
        return left.position();
    }
}
