package com.declarative.music.parser;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class LexerMock implements Lexer {
    private final Iterator<Token> tokens;
    private boolean addSemicolon;
    private Token lastToken;

    public LexerMock(final List<Token> tokens) {
        this.tokens = tokens.iterator();
        this.addSemicolon = true;
    }

    public LexerMock(final List<Token> tokens, final boolean addSemicolon) {
        this.tokens = tokens.iterator();
        this.addSemicolon = addSemicolon;
    }

    @Override
    public Token getNextToken() throws IOException {
        if (tokens.hasNext()) {
            lastToken = tokens.next();
            return lastToken;
        }
        if (addSemicolon) {
            final var newPos = new Position(lastToken.position().line(), lastToken.position().characterNumber() + 1);
            lastToken = new Token(TokenType.T_SEMICOLON, newPos, null);
            addSemicolon = false;
            return lastToken;
        }
        final var newPos = new Position(lastToken.position().line(), lastToken.position().characterNumber() + 1);
        return new Token(TokenType.T_EOF, newPos, null);
    }
}
