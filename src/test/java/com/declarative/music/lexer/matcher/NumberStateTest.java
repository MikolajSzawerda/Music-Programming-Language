package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.state.NumberState;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NumberStateTest {
    Lexer lexer = mock(Lexer.class);
    NumberState tested;

    @BeforeEach
    void init() {
        tested = new NumberState(lexer);
    }

    @Test
    void shouldParseNumber() throws IOException {
        // given
        final var expectedValue = 123;
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) '1');
        when(lexer.getNextStreamChar())
                .thenReturn((int) '2')
                .thenReturn((int) '3')
                .thenReturn(-1);
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
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) '1')
                .thenReturn((int) '.');
        when(lexer.getNextStreamChar())
                .thenReturn((int) '2')
                .thenReturn((int) '.')
                .thenReturn((int) '3')
                .thenReturn((int) '4')
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_NUMBER, new Position(0, 0), expectedValue);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}