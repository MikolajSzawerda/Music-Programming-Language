package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class PitchState extends LexerState {
    private final StringBuilder tokenBuilder;

    public PitchState(final LexerContext lexerContext) {
        super(lexerContext);
        this.tokenBuilder = new StringBuilder();
    }

    @Override
    public Token processNext() throws IOException {
        tokenBuilder.append((char) lexerContext.getCurrentStreamChar());
        final var nextChar = lexerContext.getNextStreamChar();
        var readChar = (char) nextChar;
        if (readChar == '#') {
            tokenBuilder.append(readChar);
            readChar = (char) lexerContext.getNextStreamChar();
            if (!Character.isLetterOrDigit(readChar)) {
                lexerContext.stateTransition(new IdleState(lexerContext));
                return new Token(TokenType.T_PITCH, new Position(0, 0), tokenBuilder.toString());
            }
        }
        if (nextChar == -1 || !Character.isLetterOrDigit(readChar)) {
            lexerContext.stateTransition(new IdleState(lexerContext));
            return new Token(TokenType.T_PITCH, new Position(0, 0), tokenBuilder.toString());
        }
        lexerContext.stateTransition(new IdentifierState(lexerContext, tokenBuilder));
        return null;
    }
}
