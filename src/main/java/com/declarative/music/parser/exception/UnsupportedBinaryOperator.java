package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.util.Set;

public class UnsupportedBinaryOperator extends ParsingException {
    public UnsupportedBinaryOperator(final Token token) {
        super(Set.of(TokenType.T_OPERATOR), token, "unsupported binary operator");
    }
}
