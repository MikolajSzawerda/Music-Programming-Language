package com.declarative.music.parser.production.type;

import com.declarative.music.interpreter.Visitor;
import com.declarative.music.lexer.token.Position;

import java.util.List;

public record LambdaType(List<Type> parameter, Type returnType, Position position) implements Type {
    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
