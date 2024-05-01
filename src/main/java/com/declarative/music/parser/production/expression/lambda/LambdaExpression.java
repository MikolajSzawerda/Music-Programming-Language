package com.declarative.music.parser.production.expression.lambda;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.expression.Expression;

public record LambdaExpression(Parameters parameters, String returnType, Block instructions) implements Expression {

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
