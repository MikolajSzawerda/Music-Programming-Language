package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.util.Set;


public class WrongElseStatement extends ParsingException {
    public WrongElseStatement(final Token currentToken) {
        super(Set.of(TokenType.T_IF), currentToken, "else statement without if statement");
    }
}
