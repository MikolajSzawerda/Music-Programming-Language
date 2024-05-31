package com.declarative.music.lexer;

import com.declarative.music.lexer.state.IdleState;
import com.declarative.music.lexer.state.LexerState;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;

import java.io.IOException;
import java.io.Reader;

public class LexerImpl implements LexerContext, Lexer {
    private final LexerStreamReader reader;
    private int nextStreamChar = -1;
    private LexerState currentState;

    public LexerImpl(final Reader reader) {
        this.reader = new LexerStreamReader(reader);
        currentState = new IdleState(this);
    }

    public Token getNextToken() throws IOException {
        Token nextToken = null;
        while (nextToken == null) {
            nextToken = currentState.processNext();
        }
        return nextToken;
    }


    @Override
    public void stateTransition(final LexerState newState) {
        currentState = newState;
    }

    @Override
    public int getCurrentStreamChar() {
        return nextStreamChar;
    }

    @Override
    public int getNextStreamChar() throws IOException {
        nextStreamChar = reader.read();
        return nextStreamChar;
    }

    @Override
    public Position getCurrentPosition() {
        return reader.getCurrentPosition();
    }
}
