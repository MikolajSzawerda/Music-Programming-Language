package com.declarative.music.parser.production.expression.lambda;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.type.Type;

public record LambdaExpression(Parameters parameters, Type returnType, Block instructions,
                               Position position) implements Expression {

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
