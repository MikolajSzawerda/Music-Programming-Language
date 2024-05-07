package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class CommentState extends LexerState {
    private final StringBuilder stringBuilder;

    public CommentState(final LexerContext lexerContext) {
        super(lexerContext);
        stringBuilder = new StringBuilder();
    }


    @Override
    public Token processNext() throws IOException {
        final var startPosition = lexerContext.getCurrentPosition();
        var currentChar = lexerContext.getCurrentStreamChar();
        var readChar = (char) currentChar;
        while (currentChar != -1 && readChar != '\n') {
            stringBuilder.append(readChar);
            currentChar = lexerContext.getNextStreamChar();
            readChar = (char) currentChar;
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        return new Token(TokenType.T_COMMENT, startPosition, stringBuilder.toString());
    }

}
