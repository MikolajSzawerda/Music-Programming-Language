package com.declarative.music.parser.exception;

import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;


@RequiredArgsConstructor
@Getter
public class ParsingException extends Exception {
    private final Set<TokenType> requiredTokenTypes;
    private final Token currentToken;
    private final String additionalInfo;

    public ParsingException(final Set<TokenType> types, final Token token) {
        this.requiredTokenTypes = types;
        this.currentToken = token;
        this.additionalInfo = "";
    }

    public ParsingException(final ParsingException exception) {
        this.requiredTokenTypes = exception.getRequiredTokenTypes();
        this.currentToken = exception.getCurrentToken();
        this.additionalInfo = exception.additionalInfo;
    }

    public ParsingException copy() {
        return new ParsingException(requiredTokenTypes, currentToken, additionalInfo);
    }

    @Override
    public String getMessage() {
        return "SYNTAX ERROR %s Required token: %s Token: (%s, %s) Line: %d Column: %d".formatted(additionalInfo, requiredTokenTypes,
                currentToken.type(),
                currentToken.value(),
                currentToken.position().line() + 1,
                currentToken.position().characterNumber()
        );
    }
}
