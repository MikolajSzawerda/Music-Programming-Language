package com.declarative.music.parser.exception;

import java.util.Set;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;


public class SyntaxException extends ParsingException
{
    public SyntaxException(final Set<TokenType> requiredTokenTypes,
                           final Token currentToken)
    {
        super(requiredTokenTypes, currentToken);
    }

    public SyntaxException(SyntaxException e)
    {
        super(e.getRequiredTokenTypes(), e.getCurrentToken());
    }
}
