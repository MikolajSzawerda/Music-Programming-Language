package com.declarative.music.parser.production.assign;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.expression.Expression;

public record SequenceAssignStatement(String identifier, Expression value, Position position) implements AssignStmt {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
