package com.declarative.music.parser.exception;

public class RequiredExpressionException extends ParsingException {

    public RequiredExpressionException(final ParsingException e, final String production) {
        super(e.getRequiredTokenTypes(), e.getCurrentToken(), "expected expression when parsing %s".formatted(production));
    }
}
