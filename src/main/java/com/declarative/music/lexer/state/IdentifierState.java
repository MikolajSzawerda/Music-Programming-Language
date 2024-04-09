package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.expection.MaximalIdentifierLengthException;
import com.declarative.music.lexer.terminals.KeywordsMap;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class IdentifierState extends LexerState {
    private static final int MAX_IDENTIFIER_LEN = 256;
    private final StringBuilder tokenBuilder;

    public IdentifierState(final LexerContext lexerContext) {
        super(lexerContext);
        tokenBuilder = new StringBuilder();
    }

    public IdentifierState(final LexerContext lexerContext, final StringBuilder stringBuilder) {
        super(lexerContext);
        tokenBuilder = stringBuilder;
    }


    @Override
    public Token processNext() throws IOException {
        var currentChar = lexerContext.getCurrentStreamChar();
        while (currentChar != -1 && (Character.isLetterOrDigit(currentChar) || currentChar == (int) '_')) {
            tokenBuilder.append((char) currentChar);
            currentChar = lexerContext.getNextStreamChar();
            if (tokenBuilder.length() > MAX_IDENTIFIER_LEN) {
                throw new MaximalIdentifierLengthException(lexerContext.getCurrentPosition());
            }
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        final var endPosition = lexerContext.getCurrentPosition();
        final var startPosition = new Position(endPosition.line(), endPosition.characterNumber() - tokenBuilder.length());
        return KeywordsMap.getKeywordType(tokenBuilder.toString())
                .map(keywordType -> new Token(keywordType, startPosition, null))
                .orElse(new Token(TokenType.T_IDENTIFIER, startPosition, tokenBuilder.toString()));
    }

}
