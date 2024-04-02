package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.state.CommentState;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommentStateTest {
    Lexer lexer = mock(Lexer.class);
    CommentState tested;

    @BeforeEach
    void init() {
        tested = new CommentState(lexer);
    }

    @Test
    void shouldParseComment() throws IOException {
        // given
        final var expectedValue = "hello";
        when(lexer.getCurrentStreamChar())
                .thenReturn((int) 'h');
        when(lexer.getNextStreamChar())
                .thenReturn((int) 'e')
                .thenReturn((int) 'l')
                .thenReturn((int) 'l')
                .thenReturn((int) 'o')
                .thenReturn(-1);
        final var expectedToken = new Token(TokenType.T_COMMENT, new Position(0, 0), expectedValue);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}