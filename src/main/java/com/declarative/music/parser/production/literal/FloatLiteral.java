package com.declarative.music.parser.production.literal;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record FloatLiteral(double value) implements Expression {
    @Override
    public void accept(final Visitor visitor) {

    }
}
