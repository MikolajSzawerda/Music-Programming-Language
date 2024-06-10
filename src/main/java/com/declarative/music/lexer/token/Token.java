package com.declarative.music.lexer.token;

public record Token(TokenType type, Position position, Object value) {

}
