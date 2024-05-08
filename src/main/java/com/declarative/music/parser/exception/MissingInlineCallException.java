package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.util.Set;


public class MissingInlineCallException extends ParsingException {
    public MissingInlineCallException(final Token currentToken) {
        super(Set.of(TokenType.T_IDENTIFIER), currentToken, "missing inline call after pipe operator");
    }
}
