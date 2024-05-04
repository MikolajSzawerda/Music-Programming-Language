package com.declarative.music.lexer;

import com.declarative.music.lexer.token.Token;

import java.io.IOException;

public interface Lexer {
    Token getNextToken() throws IOException;
}
