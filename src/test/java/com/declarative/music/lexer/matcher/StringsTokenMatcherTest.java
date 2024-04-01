package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class StringsTokenMatcherTest {
    @Test
    void shouldParseString() throws IOException {
        // given
        var code = "\"abcd\"";
        var expectedValue = "abcd";
        var expectedToken = new Token(TokenType.T_STRING, new Position(0, 0), expectedValue);
        var matcher = new StringsTokenMatcher(new BufferedReader(new StringReader(code)));

        // when
        var token = matcher.matchNextToken();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}