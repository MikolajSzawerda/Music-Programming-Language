package com.declarative.music.parser.exception;

import java.util.Set;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;


public class MissingParenthesisException extends ParsingException
{
    public MissingParenthesisException(Set<TokenType> tokenTypes, Token currentToken)
    {
        super(tokenTypes, currentToken, "missing closing \"%s\" parenthesis".formatted(tokenTypes));
    }

    @Override
    public ParsingException copy()
    {
        return new MissingParenthesisException(getRequiredTokenTypes(), getCurrentToken());
    }
}
