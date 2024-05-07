package com.declarative.music.parser.exception;

public class RequiredExpressionException extends ParsingException
{

    public RequiredExpressionException(ParsingException e, String production)
    {
        super(e.getRequiredTokenTypes(), e.getCurrentToken(), "expected expression when parsing %s".formatted(production));
    }
}
