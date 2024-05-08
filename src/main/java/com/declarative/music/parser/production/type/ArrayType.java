package com.declarative.music.parser.production.type;

import com.declarative.music.lexer.token.Position;

public record ArrayType(Type arrayType, Position position) implements Type {
}
