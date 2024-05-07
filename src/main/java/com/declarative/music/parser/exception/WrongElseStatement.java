package com.declarative.music.parser.exception;

import java.util.Set;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;


public class WrongElseStatement extends ParsingException
{
    public WrongElseStatement(Token currentToken)
    {
        super(Set.of(TokenType.T_IF), currentToken, "else statement without if statement");
    }
}
