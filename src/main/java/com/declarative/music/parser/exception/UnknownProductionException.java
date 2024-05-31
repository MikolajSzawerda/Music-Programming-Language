package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;

import java.util.Set;

public class UnknownProductionException extends ParsingException {
    public UnknownProductionException(final Token currentToken) {
        super(Set.of(), currentToken, "any of defined productions starts with provided token");
    }
}
