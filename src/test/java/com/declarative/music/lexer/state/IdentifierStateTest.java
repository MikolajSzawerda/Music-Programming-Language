package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class IdentifierStateTest {

    @Test
    void shouldParseIdentifier() throws IOException {
        // given
        final var code = "ab_cd";
        final var tested = new IdentifierState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_IDENTIFIER, new Position(0, 0), code);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @Test
    void shouldParseKeyword() throws IOException {
        // given
        final var code = "with";
        final var tested = new IdentifierState(new LexerContextMock(code));
        final var expectedToken = new Token(TokenType.T_WITH, new Position(0, 0), null);

        // when
        final var token = tested.processNext();

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}