package com.declarative.music.lexer;

import com.declarative.music.lexer.matcher.IdentifierTokenMatcher;
import com.declarative.music.lexer.matcher.NumberTokenMatcher;
import com.declarative.music.lexer.matcher.OperatorTokenMatcher;
import com.declarative.music.lexer.matcher.TokenMatcher;
import com.declarative.music.lexer.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class Lexer {
    private final BufferedReader reader;
    private final List<TokenMatcher> tokenMatchers;

    public Lexer(Reader reader) {
        this.reader = new BufferedReader(reader);
        tokenMatchers = List.of(
                new OperatorTokenMatcher(this.reader),
                new NumberTokenMatcher(this.reader),
                new IdentifierTokenMatcher(this.reader)
        );
    }

    public Token getNextToken() throws IOException {
        skipWhites();
        for (var matcher : tokenMatchers) {
            var result = matcher.matchNextToken();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void skipWhites() throws IOException {
        reader.mark(1024);
        int readChar;
        int i = 0;
        while ((readChar = reader.read()) != -1) {
            if (!Character.isWhitespace((char) readChar)) {
                reader.reset();
                reader.skip(i);
                return;
            }
            i++;
        }
    }

}
