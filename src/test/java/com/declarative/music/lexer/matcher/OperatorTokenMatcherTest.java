package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class OperatorTokenMatcherTest {
    @Test
    void shouldParseOperator() throws IOException {
        // given
        char code = '=';
        var expectedToken = new Token(TokenType.T_OPERATOR, new Position(0, 0), code);
        var matcher = new OperatorTokenMatcher(new BufferedReader(new StringReader(String.valueOf(code))));

        // when
        var token = matcher.matchNextToken();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}