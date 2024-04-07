package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerContextMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

class CommentOrDivisionStateTest {

    @ParameterizedTest
    @ValueSource(strings = {"hello world 1234 A#", "/", "", "\n"})
    void shouldParseComment(final String expectedValue) throws IOException {
        // given
        final var code = "//" + expectedValue;
        final var lexer = new LexerContextMock(code);
        lexer.stateTransition(new CommentOrDivisionState(lexer));
        final var expectedToken = new Token(TokenType.T_COMMENT, new Position(0, 2), expectedValue.strip());

        // when
        Token token = null;
        while (token == null) {
            token = lexer.getState().processNext();
        }

        // then
        Assertions.assertEquals(expectedToken, token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/12", "/ ", "/\n"})
    void shouldParseDivision(final String code) throws IOException {
        // given
        final var lexer = new LexerContextMock(code);
        lexer.stateTransition(new CommentOrDivisionState(lexer));
        final var expectedToken = new Token(TokenType.T_OPERATOR, new Position(0, 0), '/');

        // when
        Token token = null;
        while (token == null) {
            token = lexer.getState().processNext();
        }

        // then
        Assertions.assertEquals(expectedToken, token);
    }
}