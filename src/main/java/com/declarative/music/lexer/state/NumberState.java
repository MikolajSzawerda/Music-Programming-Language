package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class NumberState extends LexerState {


    public NumberState(final LexerContext lexerContext) {
        super(lexerContext);
    }

    @Override
    public Token processNext() throws IOException {
        final var startPosition = lexerContext.getCurrentPosition();
        final int decimals = parseNumber(lexerContext.getCurrentStreamChar()).value;
        lexerContext.stateTransition(new IdleState(lexerContext));

        if (lexerContext.getCurrentStreamChar() != '.') {
            return new Token(TokenType.T_INT_NUMBER, startPosition, decimals);
        }
        final var firstFractional = lexerContext.getNextStreamChar();
        final var fractional = parseNumber(firstFractional);
        final double value = decimals + fractional.value / Math.pow(10, fractional.length);
        return new Token(TokenType.T_FLOATING_NUMBER, startPosition, value);
    }

    private ParsedNumber parseNumber(int currentChar) throws IOException {
        int number = 0;
        int parsedLen = 0;
        while (currentChar != -1 && Character.isDigit(currentChar)) {

            number = Math.addExact(Math.multiplyExact(number, 10), Character.digit((char) currentChar, 10));
            currentChar = lexerContext.getNextStreamChar();
            parsedLen++;
        }
        return new ParsedNumber(number, parsedLen);
    }

    private record ParsedNumber(int value, int length) {
    }
}
