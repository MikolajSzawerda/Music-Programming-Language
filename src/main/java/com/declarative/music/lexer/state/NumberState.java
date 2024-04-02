package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class NumberState extends LexerState {

    private int number = 0;

    public NumberState(final LexerContext lexerContext) {
        super(lexerContext);
    }

    @Override
    public Token processNext() throws IOException {
        var currentChar = lexerContext.getCurrentStreamChar();
        while (currentChar != -1 && Character.isDigit(currentChar)) {
            number = number * 10 + Character.digit((char) currentChar, 10);
            currentChar = lexerContext.getNextStreamChar();
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        return new Token(TokenType.T_NUMBER, new Position(0, 0), number);

    }
}
