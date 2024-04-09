package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class NumberStateTest {

    @Test
    void shouldParseNumber() throws IOException {
        // given
        final var expectedValue = 123;
        final var code = "123";
        final var tested = new NumberState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_NUMBER, new Position(0, 0), expectedValue);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParseFloatingNumber() throws IOException {
        // given
        final double expectedValue = 12.34;
        final var code = "12.34";
        final var tested = new NumberState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_NUMBER, new Position(0, 0), expectedValue);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldThrow_WhenTooManyDigits() {
        final var code = "123456789123456789123456789";
        final var tested = new NumberState(new LexerContextMock(code));
        Assertions.assertThrows(ArithmeticException.class, tested::processNext);
    }
}