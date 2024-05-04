package com.declarative.music.parser;


import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class FilteredLexer implements Lexer {
    private final Lexer lexer;

    @Override
    public Token getNextToken() throws IOException {

        Token token = lexer.getNextToken();
        while (token.type() == TokenType.T_COMMENT) {
            token = lexer.getNextToken();
        }
        return token;
    }
}
