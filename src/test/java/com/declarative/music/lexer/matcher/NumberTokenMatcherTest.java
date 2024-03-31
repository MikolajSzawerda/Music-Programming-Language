package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class NumberTokenMatcherTest {
    @Test
    void shouldParseNumber() throws IOException {
        // given
        var code = 123;
        var expectedToken = new Token(TokenType.T_NUMBER, new Position(0, 0), code);
        var matcher = new NumberTokenMatcher(new BufferedReader(new StringReader(String.valueOf(code))));

        // when
        var token = matcher.matchNextToken();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}