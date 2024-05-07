package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class StringState extends LexerState {
    private final StringBuilder stringBuilder;

    public StringState(final LexerContext lexerContext) {
        super(lexerContext);
        stringBuilder = new StringBuilder();
    }


    @Override
    public Token processNext() throws IOException {
        assert lexerContext.getCurrentStreamChar() == '"';
        final var currentPosition = lexerContext.getCurrentPosition();
        var currentChar = lexerContext.getNextStreamChar();
        var readChar = (char) currentChar;
        var escape = false;
        while (currentChar != -1 && (escape || readChar != '"')) {
            stringBuilder.append(readChar);
            currentChar = lexerContext.getNextStreamChar();
            readChar = (char) currentChar;
            escape = (readChar == '\\');
            if (escape) {
                currentChar = lexerContext.getNextStreamChar();
                readChar = switch ((char) currentChar) {
                    case 'n':
                        yield '\n';
                    case 't':
                        yield '\t';
                    default:
                        yield (char) currentChar;
                };
            }
        }
        if (currentChar != -1) {
            lexerContext.getNextStreamChar();
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        return new Token(TokenType.T_STRING, currentPosition, stringBuilder.toString());
    }

}
