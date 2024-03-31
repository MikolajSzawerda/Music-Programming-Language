package com.declarative.music.lexer.matcher;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;

@RequiredArgsConstructor
public abstract class AbstractTokenMatcher implements TokenMatcher {
    protected final BufferedReader reader;
}
