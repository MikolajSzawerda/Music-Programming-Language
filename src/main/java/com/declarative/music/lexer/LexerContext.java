package com.declarative.music.lexer;

import com.declarative.music.lexer.state.LexerState;
import com.declarative.music.lexer.token.Position;

import java.io.IOException;

public interface LexerContext {
    void stateTransition(LexerState newState);

    int getCurrentStreamChar();

    int getNextStreamChar() throws IOException;

    Position getCurrentPosition();
}
