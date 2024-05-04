package com.declarative.music.lexer.utils;

import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class LexerUtils {
    private LexerUtils() {

    }

    public static List<Token> getAllTokens(final LexerImpl lexer) throws IOException {
        final List<Token> tokens = new LinkedList<>();
        Token token;
        while ((token = lexer.getNextToken()).type() != TokenType.T_EOF) {
            tokens.add(token);
        }
        return tokens;
    }
}
