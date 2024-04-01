package com.declarative.music.lexer.matcher;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;

@RequiredArgsConstructor
//TODO think about common structure
public abstract class AbstractTokenMatcher implements TokenMatcher {
    protected final BufferedReader reader;
}
