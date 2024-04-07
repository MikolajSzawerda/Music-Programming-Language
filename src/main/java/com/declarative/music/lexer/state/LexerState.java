package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.token.Token;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class LexerState {
    protected final LexerContext lexerContext;

    public abstract Token processNext() throws IOException;
}
