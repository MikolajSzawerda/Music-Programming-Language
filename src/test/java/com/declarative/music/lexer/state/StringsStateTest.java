package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StringsStateTest {
    @Test
    void shouldParseString() throws IOException {
        // given
        final var code = "\"a2d\"";
        final var tested = new StringState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_STRING, new Position(0, 0), "a2d");

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldHandleEscapeForQuote() throws IOException {
        // given
        final var code = "\"a\\\"b\"";
        final var tested = new StringState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_STRING, new Position(0, 0), "a\"b");

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}