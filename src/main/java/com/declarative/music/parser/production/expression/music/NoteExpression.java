package com.declarative.music.parser.production.expression.music;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.expression.Expression;

public record NoteExpression(String pitch, Expression octave, String duration) implements Expression {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
