package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.util.Set;


public class SyntaxException extends ParsingException {
    public SyntaxException(final Set<TokenType> requiredTokenTypes,
                           final Token currentToken) {
        super(requiredTokenTypes, currentToken);
    }

    public SyntaxException(final SyntaxException e) {
        super(e.getRequiredTokenTypes(), e.getCurrentToken());
    }
}
