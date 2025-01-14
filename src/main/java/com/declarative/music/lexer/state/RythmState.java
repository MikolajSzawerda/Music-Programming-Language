package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class RythmState extends LexerState {
    private final StringBuilder tokenBuilder;

    public RythmState(final LexerContext lexerContext) {
        super(lexerContext);
        this.tokenBuilder = new StringBuilder();
    }

    @Override
    public Token processNext() throws IOException {
        final var currentChar = (char) lexerContext.getCurrentStreamChar();
        final var startPosition = lexerContext.getCurrentPosition();
        tokenBuilder.append(currentChar);
        var nextChar = lexerContext.getNextStreamChar();
        var readChar = (char) nextChar;
        if (currentChar == 'd' && readChar != 'l') {
            if (Character.isLetterOrDigit(readChar)) {
                tokenBuilder.append(readChar);
                lexerContext.getNextStreamChar();
            }
            lexerContext.stateTransition(new IdentifierState(lexerContext, tokenBuilder));
            return null;
        }
        if (currentChar == 'd') {
            tokenBuilder.append(readChar);
            nextChar = lexerContext.getNextStreamChar();
            readChar = (char) nextChar;
        }

        if (readChar == '_') {
            tokenBuilder.append(readChar);
            readChar = (char) lexerContext.getNextStreamChar();
            if (readChar == 'd' || readChar == 't') {
                tokenBuilder.append(readChar);
                readChar = (char) lexerContext.getNextStreamChar();
                if (!Character.isLetterOrDigit(readChar)) {
                    lexerContext.stateTransition(new IdleState(lexerContext));
                    return new Token(TokenType.T_RHYTHM, startPosition, tokenBuilder.toString());
                }
            }
            lexerContext.stateTransition(new IdentifierState(lexerContext, tokenBuilder));
            return null;
        }
        if (nextChar == -1 || !Character.isLetterOrDigit(readChar)) {
            lexerContext.stateTransition(new IdleState(lexerContext));
            return new Token(TokenType.T_RHYTHM, startPosition, tokenBuilder.toString());
        }
        lexerContext.stateTransition(new IdentifierState(lexerContext, tokenBuilder));
        return null;
    }
}
