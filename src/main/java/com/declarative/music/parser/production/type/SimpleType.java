package com.declarative.music.parser.production.type;

import com.declarative.music.lexer.token.Position;

public record SimpleType(Types type, Position position) implements Type {
}
