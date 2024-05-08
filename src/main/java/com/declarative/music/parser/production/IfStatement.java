package com.declarative.music.parser.production;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.expression.Expression;

public record IfStatement(Expression condition, Block instructions, IfStatement otherwise,
                          Position position) implements Statement {
    public IfStatement(final Expression condition, final Block instructions, final Position position) {
        this(condition, instructions, null, position);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
