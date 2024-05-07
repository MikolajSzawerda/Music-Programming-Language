package com.declarative.music.parser.exception;

import java.util.Set;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;


public class MissingInlineCallException extends ParsingException
{
    public MissingInlineCallException(Token currentToken)
    {
        super(Set.of(TokenType.T_IDENTIFIER), currentToken, "missing inline call after pipe operator");
    }
}
