package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.state.IdentifierState;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdentifierStateTest {
    Lexer lexer = mock(Lexer.class);
    IdentifierState tested;

    @BeforeEach
    void init() {
        tested = new IdentifierState(lexer);
    }

    @Test
    void shouldParseIdentifier() throws IOException {
        // given
        final var expectedValue = "ab12";
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) 'a');
        when(lexer.getNextStreamChar())
                .thenReturn((int) 'b')
                .thenReturn((int) '1')
                .thenReturn((int) '2')
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_IDENTIFIER, new Position(0, 0), expectedValue);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParseKeyword() throws IOException {
        // given
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) 'w');
        when(lexer.getNextStreamChar())
                .thenReturn((int) 'i')
                .thenReturn((int) 't')
                .thenReturn((int) 'h')
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_WITH, new Position(0, 0), null);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}