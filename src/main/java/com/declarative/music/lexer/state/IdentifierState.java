package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class IdentifierState extends LexerState {
    private final StringBuilder tokenBuilder;
    private final boolean completed = false;

    public IdentifierState(final LexerContext lexerContext) {
        super(lexerContext);
        tokenBuilder = new StringBuilder();
    }


    @Override
    public Token processNext() throws IOException {
        var currentChar = lexerContext.getCurrentStreamChar();
        while (currentChar != -1 && Character.isLetterOrDigit(currentChar)) {
            tokenBuilder.append((char) currentChar);
            currentChar = lexerContext.getNextStreamChar();
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        return new Token(TokenType.T_IDENTIFIER, new Position(0, 0), tokenBuilder.toString());
    }

}
