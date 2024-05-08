package com.declarative.music.parser.production.expression.array;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.expression.Expression;

public record RangeExpression(Expression start, Expression end) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Position position() {
        return start.position();
    }

}
