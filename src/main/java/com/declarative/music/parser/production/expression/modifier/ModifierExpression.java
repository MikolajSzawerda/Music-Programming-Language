package com.declarative.music.parser.production.expression.modifier;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.expression.Expression;

public record ModifierExpression(Expression modified, Modifier modifier, Position position) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

}
