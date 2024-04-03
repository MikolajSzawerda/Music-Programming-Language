package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.state.OperatorOrUnknownState;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperatorStateTest {
    Lexer lexer = mock(Lexer.class);
    OperatorOrUnknownState tested;

    @BeforeEach
    void init() {
        tested = new OperatorOrUnknownState(lexer);
    }

    @Test
    void shouldParseSingleOperator() throws IOException {
        // given
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) '+');
        when(lexer.getNextStreamChar())
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_OPERATOR, new Position(0, 0), "+");

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParseDoubleOperator() throws IOException {
        // given
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) '+');
        when(lexer.getNextStreamChar())
                .thenReturn((int) '=')
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_OPERATOR, new Position(0, 0), "+=");

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParsePunctuation() throws IOException {
        // given
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) '{');
        final var expectedToken = new Token(TokenType.T_L_CURL_PARENTHESIS, new Position(0, 0), null);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}