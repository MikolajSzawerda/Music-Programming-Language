package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class OperatorOrUnknownState extends LexerState {
    public OperatorOrUnknownState(final LexerContext lexerContext) {
        super(lexerContext);
    }


    @Override
    public Token processNext() throws IOException {
        final var currentChar = (char) lexerContext.getCurrentStreamChar();
        lexerContext.stateTransition(new IdleState(lexerContext));

        if (currentChar == '=') {
            lexerContext.getNextStreamChar();
            return new Token(TokenType.T_OPERATOR, new Position(0, 0), currentChar);
        }
        lexerContext.getNextStreamChar();
        return null;
    }

}
