package com.declarative.music.lexer.state;

import java.io.IOException;

public interface LexerContext {
    void stateTransition(LexerState newState);

    int getCurrentStreamChar();

    int getNextStreamChar() throws IOException;
}
