package com.declarative.music.parser.production.assign;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.Statement;
import com.declarative.music.parser.production.expression.Expression;

public record SequenceAssignStatement(String identifier, Expression value) implements Statement {
    @Override
    public void accept(final Visitor visitor) {

    }
}
