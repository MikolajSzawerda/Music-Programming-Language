package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.state.StringState;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StringsStateTest {
    Lexer lexer = mock(Lexer.class);
    StringState tested;

    @BeforeEach
    void init() {
        tested = new StringState(lexer);
    }

    @Test
    void shouldParseString() throws IOException {
        // given
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) '"');
        when(lexer.getNextStreamChar())
                .thenReturn((int) 'a')
                .thenReturn((int) '2')
                .thenReturn((int) 'd')
                .thenReturn((int) '"')
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_STRING, new Position(0, 0), "a2d");

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}