package com.declarative.music.parser.production.expression.pipe;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.expression.Expression;

import java.util.List;

public record InlineFuncCall(String name, List<Expression> arguments, Position position) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
