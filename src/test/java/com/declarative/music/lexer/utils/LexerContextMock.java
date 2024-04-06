package com.declarative.music.lexer.utils;

import com.declarative.music.lexer.state.LexerContext;
import com.declarative.music.lexer.state.LexerState;

public class LexerContextMock implements LexerContext {
    private final String code;
    private int idx = 0;
    private LexerState currentState;

    public LexerContextMock(final String code) {
        this.code = code;
    }

    public LexerState getState() {
        return currentState;
    }

    @Override
    public void stateTransition(final LexerState newState) {
        currentState = newState;
    }

    @Override
    public int getCurrentStreamChar() {
        if (idx >= code.length())
            return -1;
        return code.charAt(idx);
    }

    @Override
    public int getNextStreamChar() {
        idx++;
        return getCurrentStreamChar();
    }
}
