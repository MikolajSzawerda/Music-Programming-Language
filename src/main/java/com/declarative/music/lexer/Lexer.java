package com.declarative.music.lexer;

import com.declarative.music.lexer.state.IdleState;
import com.declarative.music.lexer.state.LexerContext;
import com.declarative.music.lexer.state.LexerState;
import com.declarative.music.lexer.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class Lexer implements LexerContext {
    private final BufferedReader reader;
    private int nextStreamChar = -1;
    private LexerState currentState;

    public Lexer(final Reader reader) {
        this.reader = new BufferedReader(reader);
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
}
