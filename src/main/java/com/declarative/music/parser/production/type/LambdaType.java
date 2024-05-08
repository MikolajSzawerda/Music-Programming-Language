package com.declarative.music.parser.production.type;

import com.declarative.music.lexer.token.Position;

import java.util.List;

public record LambdaType(List<Type> parameter, Type returnType, Position position) implements Type {
}
