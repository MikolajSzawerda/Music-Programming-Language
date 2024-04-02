package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
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
        var currentChar = lexerContext.getCurrentStreamChar();
        var readChar = (char) currentChar;
        while (currentChar != -1 && readChar != '\n') {
            stringBuilder.append(readChar);
            currentChar = lexerContext.getNextStreamChar();
            readChar = (char) currentChar;
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        return new Token(TokenType.T_COMMENT, new Position(0, 0), stringBuilder.toString());
    }

}
