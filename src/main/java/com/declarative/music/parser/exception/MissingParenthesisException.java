package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.util.Set;


public class MissingParenthesisException extends ParsingException {
    public MissingParenthesisException(final Set<TokenType> tokenTypes, final Token currentToken) {
        super(tokenTypes, currentToken, "missing closing \"%s\" parenthesis".formatted(tokenTypes));
    }

    @Override
    public ParsingException copy() {
        return new MissingParenthesisException(getRequiredTokenTypes(), getCurrentToken());
    }
}
